/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testOnly.controllers

import config.FrontendAppConfig
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import testOnly.connectors.TestOnlyEmailVerificationConnector
import testOnly.forms.StartForm
import testOnly.models.VerifyEmailRequest
import testOnly.views.TestOnlyViews
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URLEncoder
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StubVerificationController @Inject() (
  appConfig:         FrontendAppConfig,
  connector:         TestOnlyEmailVerificationConnector,
  views:             TestOnlyViews,
  val authConnector: AuthConnector,
  mcc:               MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with AuthorisedFunctions {

  private val redirectToAuthStub: Result = {

    val continueUrl = appConfig.selfUrl + testOnly.controllers.routes.StubVerificationController.showStartForm().url
    val authLoginStubPath = s"/auth-login-stub/gg-sign-in?continue=${URLEncoder.encode(continueUrl, Charset.forName("UTF-8"))}"
    val authLoginHost =
      appConfig.configuration.getOptional[String]("platform.frontend.host").getOrElse("http://localhost:9949")

    Redirect(authLoginHost + authLoginStubPath)
  }

  def showStartForm(): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Ok(views.start(StartForm.form)))
    }.recover { case _: AuthorisationException =>
      redirectToAuthStub
    }
  }

  def submitVerificationRequest(): Action[AnyContent] = Action.async { implicit request =>
    authorised()
      .retrieve(Retrievals.credentials) {
        case Some(creds) =>
          StartForm.form
            .bindFromRequest()
            .fold[Future[Result]](
              startFormWithErrors => Future.successful(BadRequest(views.start(startFormWithErrors))),
              validStartForm => {

                for {
                  redirectUrl <- connector.requestEmailVerification(VerifyEmailRequest.fromStartForm(creds.providerId, validStartForm))
                  passcodes   <- connector.getTestOnlyPasscodes()
                } yield {
                  Ok(views.verificationResponse(appConfig.selfUrl + redirectUrl, passcodes))
                }
              }
            )
        case None => Future.successful(redirectToAuthStub)
      }
      .recover { case _: AuthorisationException =>
        redirectToAuthStub
      }
  }

  def checkStatus(): Action[AnyContent] = Action.async { implicit request =>

    authorised()
      .retrieve(Retrievals.credentials) {
        case Some(creds) =>
          connector
            .verificationStatus(creds.providerId)
            .map { verificationStatusResponse =>
              Ok(views.status(verificationStatusResponse.emails))
            }
            .recover {
              case UpstreamErrorResponse(_, 404, _, _) =>
                NotFound(views.error("Not Found", "Not Found", "No email verification status found for your credId"))
              case e: Exception =>
                InternalServerError(
                  views.error("Internal Server Error", "Internal Server Error", "Error fetching verification status: " + e.getMessage)
                )
            }
        case None => Future.successful(redirectToAuthStub)
      }
      .recover { case _: AuthorisationException =>
        redirectToAuthStub
      }
  }
}
