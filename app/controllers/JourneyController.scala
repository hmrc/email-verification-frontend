/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.{ErrorHandler, FrontendAppConfig}
import connectors.EmailVerificationConnector
import models.{EmailForm, ResendPasscodeResponse, SubmitEmailResponse, ValidatePasscodeResponse}
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Environment, Mode}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, PermitAllOnDev, RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Views

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyController @Inject() (
  emailVerificationConnector: EmailVerificationConnector,
  views:                      Views,
  errorHandler:               ErrorHandler,
  cc:                         MessagesControllerComponents,
  environment:                Environment
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(cc) {

  def enterEmail(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    emailVerificationConnector.getJourney(journeyId).flatMap {
      case Some(journey) =>
        Future.successful(
          Ok(
            views.emailForm(
              EmailForm.form.fill(EmailForm("", continueUrl.unsafeValue)),
              routes.JourneyController.submitEmail(journeyId, continueUrl, origin),
              Some(journey)
            )
          )
        )
      case None =>
        errorHandler.notFoundTemplate.map(NotFound(_))
    }
  }

  def submitEmail(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    EmailForm.form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          emailVerificationConnector.getJourney(journeyId).flatMap {
            case Some(journey) =>
              Future.successful(
                BadRequest(views.emailForm(formWithErrors, routes.JourneyController.submitEmail(journeyId, continueUrl, origin), Some(journey)))
              )
            case None =>
              errorHandler.notFoundTemplate.map(NotFound(_))
          },
        form =>
          emailVerificationConnector.submitEmail(journeyId, form.email).flatMap {
            case SubmitEmailResponse.Accepted =>
              Future.successful(
                Redirect(routes.JourneyController.enterPasscode(journeyId, continueUrl, origin, None))
              )
            case SubmitEmailResponse.JourneyNotFound =>
              errorHandler.notFoundTemplate.map(NotFound(_))
            case SubmitEmailResponse.TooManyAttempts(continueUrl) =>
              val validated = RedirectUrl(continueUrl)
                .get(OnlyRelative | PermitAllOnDev(environment))
                .url
              Future.successful(
                Forbidden(views.emailLimitReached(validated))
              )
          }
      )
  }

  def enterPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String, requestedNew: Option[Boolean]): Action[AnyContent] = Action.async {
    implicit request =>
      emailVerificationConnector.getJourney(journeyId).flatMap {
        case Some(journey) =>
          Future.successful(
            Ok(
              views.hybridPasscodeForm(
                passcodeForm,
                journeyId,
                continueUrl,
                origin,
                journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
                journey,
                requestedNew.getOrElse(false)
              )
            )
          )
        case None =>
          errorHandler.notFoundTemplate.map(NotFound(_))
      }
  }

  def resendPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>

    emailVerificationConnector.resendPasscode(journeyId).flatMap {
      case ResendPasscodeResponse.PasscodeResent =>
        Future.successful(
          Redirect(routes.JourneyController.enterPasscode(journeyId, continueUrl, origin, Some(true)))
        )
      case ResendPasscodeResponse.TooManyAttemptsForEmail(journey) =>
        val validated = continueUrl.get(OnlyRelative).url
        Future.successful(Redirect(validated))
      case ResendPasscodeResponse.TooManyAttemptsInSession(continueUrl) =>
        val validated = RedirectUrl(continueUrl)
          .get(OnlyRelative | PermitAllOnDev(environment))
          .url
        Future.successful(Redirect(validated))
      case ResendPasscodeResponse.JourneyNotFound =>
        errorHandler.notFoundTemplate.map(NotFound(_))
      case ResendPasscodeResponse.NoEmailProvided =>
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }
  }

  def submitPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    passcodeForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          emailVerificationConnector.getJourney(journeyId).flatMap {
            case Some(journey) =>
              Future.successful(
                BadRequest(
                  views.hybridPasscodeForm(
                    formWithErrors,
                    journeyId,
                    continueUrl,
                    origin,
                    journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
                    journey
                  )
                )
              )
            case None =>
              errorHandler.notFoundTemplate.map(NotFound(_))
          },
        passcode =>
          emailVerificationConnector.validatePasscode(journeyId, passcode).flatMap {
            case ValidatePasscodeResponse.Complete(redirectUri) =>
              val isLocalDevMachine: Boolean = appConfig.isAppRunningOnLocalDevMachine
              val policy =
                if (environment.mode == Mode.Test) UnsafePermitAll
                else if (isLocalDevMachine) UnsafePermitAll
                else OnlyRelative | PermitAllOnDev(environment)
              val validated = RedirectUrl(redirectUri).get(policy).url
              Future.successful(Redirect(validated))
            case ValidatePasscodeResponse.IncorrectPasscode(journey) =>
              Future.successful(
                BadRequest(
                  views.hybridPasscodeForm(
                    passcodeForm.withError("passcode", "passcodeform.error.wrongPasscode"),
                    journeyId,
                    continueUrl,
                    origin,
                    journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
                    journey
                  )
                )
              )
            case ValidatePasscodeResponse.JourneyNotFound =>
              errorHandler.notFoundTemplate.map(NotFound(_))
            case ValidatePasscodeResponse.TooManyAttempts(continueUrl) =>
              val isLocalDevMachine: Boolean = appConfig.isAppRunningOnLocalDevMachine
              val policy =
                if (environment.mode == Mode.Test) UnsafePermitAll
                else if (isLocalDevMachine) UnsafePermitAll
                else OnlyRelative | PermitAllOnDev(environment)
              val validated = RedirectUrl(continueUrl).get(policy).url

              Future.successful(Redirect(validated))
          }
      )
  }

  private def passcodeForm: Form[String] = Form(
    Forms.single(
      "passcode" -> text.verifying("passcodeform.error.invalidFormat", _.matches("^[BCDFGHJKLMNPQRSTVWXYZ]{6}$"))
    )
  )
}
