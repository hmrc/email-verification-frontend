/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{EmailForm, EmailPasscodeException, PasscodeForm}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders.{RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Views

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailPasscodeController @Inject() (
  views: Views,
  emailVerificationConnector: EmailVerificationConnector,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
  extends FrontendController(mcc) with Logging {

  def showEmailForm(continue: RedirectUrl): Action[AnyContent] = Action { implicit request =>
    Ok(views.emailForm(
      EmailForm.form.fill(EmailForm("", continue.get(UnsafePermitAll).url)),
      routes.EmailPasscodeController.submitEmailForm()
    ))
  }

  def submitEmailForm(): Action[AnyContent] = Action.async { implicit request =>
    val langCookieValue = request.cookies.get(request.messagesApi.langCookieName).map(_.value).getOrElse("en")

    EmailForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.emailForm(formWithErrors, controllers.routes.EmailPasscodeController.submitEmailForm()))),
      emailForm => {
        val obfuscatedEmailAddress = emailForm.email.take(4) + "..." + emailForm.email.takeRight(4)
        val forwardedFor = request.headers.get(HeaderNames.X_FORWARDED_FOR).fold("") { fwd => s"x_forwarded_for: $fwd" }

        emailVerificationConnector.requestPasscode(emailForm.email, langCookieValue).map { _ =>
          logger.info(s"Passcode sent to email address $obfuscatedEmailAddress. $forwardedFor")

          Ok(views.passcodeForm(
            PasscodeForm.form.fill(PasscodeForm(emailForm.email, "", emailForm.continue))
          ))
        }.recoverWith {
          case e: EmailPasscodeException.MissingSessionId => {
            logger.warn(s"Missing sessionId. $forwardedFor")
            Future.successful(Unauthorized(errorHandler.internalServerErrorTemplate))
          }
          case e: EmailPasscodeException.MaxNewEmailsExceeded => {
            logger.info(s"Max permitted number of emails reached. $forwardedFor")
            Future.successful(Redirect(routes.EmailPasscodeController.showEmailLimitReached(RedirectUrl(emailForm.continue))))
          }
          case e: EmailPasscodeException.EmailAlreadyVerified => {
            logger.info(s"Email $obfuscatedEmailAddress already verified. $forwardedFor")
            Future.successful(Redirect(routes.EmailPasscodeController.showEmailAlreadyVerified(RedirectUrl(emailForm.continue))))
          }
          case e: EmailPasscodeException.EmailVerificationServerError => {
            logger.error(s"Request to email-verification to send passcode to email $obfuscatedEmailAddress failed. $forwardedFor", e)
            Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }
        }
      }
    )
  }

  def submitPasscodeForm(): Action[AnyContent] = Action.async { implicit request =>
    PasscodeForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(views.passcodeForm(formWithErrors))),
      passcodeForm => {
        val forwardedFor = request.headers.get(HeaderNames.X_FORWARDED_FOR).fold("") { fwd => s"x_forwarded_for: $fwd" }
        val obfuscatedEmailAddress = passcodeForm.email.take(4) + "..." + passcodeForm.email.takeRight(4)
        emailVerificationConnector.verifyPasscode(passcodeForm.email, passcodeForm.passcode).map { _ =>
          logger.info(s"Email passcode for $obfuscatedEmailAddress verified")

          Redirect(routes.EmailPasscodeController.showSuccess(RedirectUrl(passcodeForm.continue)))
        }.recoverWith {
          case e: EmailPasscodeException.MissingSessionId => {
            logger.warn(s"Missing sessionId. $forwardedFor")
            Future.successful(Unauthorized(errorHandler.internalServerErrorTemplate))
          }
          case e: EmailPasscodeException.IncorrectPasscode => {
            logger.info(s"Passcode supplied for email $obfuscatedEmailAddress was incorrect. $forwardedFor")
            Future.successful(BadRequest(views.passcodeForm(PasscodeForm.form
              .fill(passcodeForm.copy(passcode = ""))
              .withError("passcode", request.messages("passcodeform.error.wrongPasscode")))))
          }
          case e: EmailPasscodeException.MaxPasscodeAttemptsExceeded => {
            logger.info(s"Max permitted number of passcode attempts reached. $forwardedFor")
            Future.successful(Redirect(routes.EmailPasscodeController.showPasscodeLimitReached(RedirectUrl(passcodeForm.continue))))
          }
          case e: EmailPasscodeException.EmailVerificationServerError => {
            logger.error(s"Request to email-verification to verify passcode for email $obfuscatedEmailAddress failed. $forwardedFor", e)
            Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }

        }
      }
    )
  }

  def showSuccess(continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.success(
      buttonUrl = continue.unsafeValue
    )))
  }

  def showPasscodeLimitReached(continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.passcodeLimitReached(
      buttonUrl = continue.unsafeValue
    )))
  }

  def showEmailLimitReached(continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.emailLimitReached(
      buttonUrl = continue.unsafeValue
    )))
  }

  def showEmailAlreadyVerified(continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.emailAlreadyVerified(
      buttonUrl = continue.unsafeValue
    )))
  }

}
