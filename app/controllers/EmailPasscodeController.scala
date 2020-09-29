/*
 * Copyright 2020 HM Revenue & Customs
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
import crypto.Decrypter
import javax.inject.{Inject, Singleton}
import models.{EmailForm, EmailPasscodeException, PasscodeForm}
import play.api.Logging
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader, Result}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.play.bootstrap.binders.{RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Views
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class EmailPasscodeController @Inject() (
  views: Views,
  emailVerificationConnector: EmailVerificationConnector,
  decrypter: Decrypter,
  mcc: MessagesControllerComponents,
  messages: MessagesApi,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
  extends FrontendController(mcc) with Logging {

  implicit def lang(implicit rh: RequestHeader): Lang = rh.cookies.get("PLAY_LANG").fold[Lang](Lang("en"))(c => Lang(c.value))

  def showEmailForm(continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.emailForm(EmailForm.form.fill(EmailForm("", continue.get(UnsafePermitAll).url)))))
  }

  def submitEmailForm(): Action[AnyContent] = Action.async { implicit request =>

    EmailForm.form.bindFromRequest().fold[Future[Result]](
      formWithErrors => Future.successful(BadRequest(views.emailForm(formWithErrors))),
      emailForm => {
        val obfuscatedEmailAddress = emailForm.email.take(4) + "..." + emailForm.email.takeRight(4)
        val forwardedFor = request.headers.get(HeaderNames.X_FORWARDED_FOR).fold("") { fwd => s"x_forwarded_for: $fwd" }
        emailVerificationConnector.requestPasscode(emailForm.email).flatMap { response =>
          logger.info(s"Passcode sent to email address $obfuscatedEmailAddress. $forwardedFor")
          Try(Ok(views.passcodeForm(PasscodeForm.form.fill(PasscodeForm(emailForm.email, "", emailForm.continue))))) match {
            case Success(renderedPasscodeForm) => Future.successful(renderedPasscodeForm)
            case Failure(e) => {
              logger.error("Failed to build passcodeform view", e)
              Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
            }
          }
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

  def showPasscodeForm(email: String, continue: RedirectUrl): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.passcodeForm(PasscodeForm.form.fill(PasscodeForm(email, "", continue.get(UnsafePermitAll).url)))))
  }

  def submitPasscodeForm(): Action[AnyContent] = Action.async { implicit request =>
    PasscodeForm.form.bindFromRequest().fold[Future[Result]](
      formWithErrors => Future.successful(BadRequest(views.passcodeForm(formWithErrors))),
      passcodeForm => {
        val forwardedFor = request.headers.get(HeaderNames.X_FORWARDED_FOR).fold("") { fwd => s"x_forwarded_for: $fwd" }
        val obfuscatedEmailAddress = passcodeForm.email.take(4) + "..." + passcodeForm.email.takeRight(4)
        emailVerificationConnector.verifyPasscode(passcodeForm.email, passcodeForm.passcode).flatMap { _ =>
          logger.info(s"Email passcode for $obfuscatedEmailAddress verified")
          Future.successful(Redirect(routes.EmailPasscodeController.showSuccess(RedirectUrl(passcodeForm.continue))))
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
