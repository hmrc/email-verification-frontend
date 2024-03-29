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

import connectors.EmailVerificationConnector
import crypto.Decrypter

import javax.inject.{Inject, Singleton}
import java.time.ZonedDateTime
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.crypto.Crypted
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

case class Token(token: String, continueUrl: String)

object Token {
  implicit val tokenReads: Reads[Token] = Json.reads[Token]
}

@Singleton
class EmailVerificationController @Inject() (
  emailVerificationConnector: EmailVerificationConnector,
  decrypter:                  Decrypter,
  mcc:                        MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) {

  def dateTimeProvider: ZonedDateTime = ZonedDateTime.now()

  private def decodeToken(token: String): Array[Byte] = {
    if (token.contains('+') || token.contains('/') || token.contains('=')) {
      Base64.getDecoder.decode(token.getBytes("UTF-8"))
    } else {
      Base64.getUrlDecoder.decode(token.getBytes("UTF-8"))
    }
  }

  def verify(token: String): Action[AnyContent] = Action.async { implicit request =>
    val redirectToContinue = for {
      decryptedToken <- Future.fromTry(decrypter.decryptAs[Token](Crypted(new String(decodeToken(token)))))
      _              <- emailVerificationConnector.verifyEmailAddress(decryptedToken.token)
    } yield Redirect(decryptedToken.continueUrl)

    redirectToContinue.recover { case _ => Redirect(routes.ErrorController.showErrorPage) }
  }

}
