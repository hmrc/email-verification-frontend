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
import connectors.{EmailVerificationConnector, ResendPasscodeResponse, SubmitEmailResponse, ValidatePasscodeResponse}
import models.EmailForm
import play.api.{Environment, Mode}
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, PermitAllOnDev, RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Views

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class JourneyController @Inject() (
  emailVerificationConnector: EmailVerificationConnector,
  views: Views,
  errorHandler: ErrorHandler,
  cc: MessagesControllerComponents,
  environment: Environment
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(cc) {

  def enterEmail(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    emailVerificationConnector.getJourney(journeyId).map {
      case Some(journey) =>
        Ok(views.emailForm(
          EmailForm.form,
          routes.JourneyController.submitEmail(journeyId, continueUrl, origin),
          Some(journey)
        ))
      case None =>
        NotFound(errorHandler.notFoundTemplate)
    }
  }

  def submitEmail(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    EmailForm.form.bindFromRequest().fold(
      formWithErrors =>
        emailVerificationConnector.getJourney(journeyId).map {
          case Some(journey) =>
            BadRequest(views.emailForm(formWithErrors, routes.JourneyController.submitEmail(journeyId, continueUrl, origin), Some(journey)))
          case None =>
            NotFound(errorHandler.notFoundTemplate)
        },
      form =>
        emailVerificationConnector.submitEmail(journeyId, form.email).map {
          case SubmitEmailResponse.Accepted =>
            Redirect(routes.JourneyController.enterPasscode(journeyId, continueUrl, origin))
          case SubmitEmailResponse.JourneyNotFound =>
            NotFound(errorHandler.notFoundTemplate)
          case SubmitEmailResponse.TooManyAttempts(continueUrl) =>
            val validated = RedirectUrl(continueUrl)
              .get(OnlyRelative | PermitAllOnDev(environment))
              .url

            Forbidden(views.emailLimitReached(validated))
        }
    )
  }

  def enterPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    emailVerificationConnector.getJourney(journeyId).map {
      case Some(journey) =>
        Ok(views.hybridPasscodeForm(
          passcodeForm,
          journeyId,
          continueUrl,
          origin,
          journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
          journey
        ))
      case None =>
        NotFound(errorHandler.notFoundTemplate)
    }
  }

  def resendPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>

    emailVerificationConnector.resendPasscode(journeyId).map {
      case ResendPasscodeResponse.PasscodeResent =>
        Redirect(routes.JourneyController.enterPasscode(journeyId, continueUrl, origin))
      case ResendPasscodeResponse.TooManyAttemptsForEmail(journey) =>
        val validated = continueUrl.get(OnlyRelative).url
        Redirect(validated)
      case ResendPasscodeResponse.TooManyAttemptsInSession(continueUrl) =>
        val validated = RedirectUrl(continueUrl)
          .get(OnlyRelative | PermitAllOnDev(environment))
          .url
        Redirect(validated)
      case ResendPasscodeResponse.JourneyNotFound =>
        NotFound(errorHandler.notFoundTemplate)
      case ResendPasscodeResponse.NoEmailProvided =>
        InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }

  def submitPasscode(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    passcodeForm.bindFromRequest().fold(
      formWithErrors =>
        emailVerificationConnector.getJourney(journeyId).map {
          case Some(journey) =>
            BadRequest(views.hybridPasscodeForm(
              formWithErrors,
              journeyId,
              continueUrl,
              origin,
              journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
              journey
            ))
          case None =>
            NotFound(errorHandler.notFoundTemplate)
        },
      passcode =>
        emailVerificationConnector.validatePasscode(journeyId, passcode).map {
          case ValidatePasscodeResponse.Complete(redirectUri) =>
            val allowRelativeUrls: Boolean = appConfig.allowRelativeUrls

            val policy = if (environment.mode == Mode.Test)
              UnsafePermitAll
            else if (allowRelativeUrls) UnsafePermitAll
            else
              OnlyRelative | PermitAllOnDev(environment)

            val validated = RedirectUrl(redirectUri)
              .get(policy)
              .url

            Redirect(validated)
          case ValidatePasscodeResponse.IncorrectPasscode(journey) =>
            BadRequest(views.hybridPasscodeForm(
              passcodeForm.withError("passcode", "passcodeform.error.wrongPasscode"),
              journeyId,
              continueUrl,
              origin,
              journey.enterEmailUrl.getOrElse(routes.JourneyController.enterEmail(journeyId, continueUrl, origin).url),
              journey
            ))
          case ValidatePasscodeResponse.JourneyNotFound =>
            NotFound(errorHandler.notFoundTemplate)
          case ValidatePasscodeResponse.TooManyAttempts(continueUrl) =>
            val validated = RedirectUrl(continueUrl)
              .get(OnlyRelative | PermitAllOnDev(environment))
              .url
            Redirect(validated)
        }
    )
  }

  private def passcodeForm: Form[String] = Form(Forms.single(
    "passcode" -> text.verifying("passcodeform.error.invalidFormat", _.matches("^[BCDFGHJKLMNPQRSTVWXYZ]{6}$"))
  ))
}
