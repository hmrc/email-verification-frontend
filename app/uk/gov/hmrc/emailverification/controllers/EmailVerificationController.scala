/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.emailverification.controllers

import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Action
import uk.gov.hmrc.emailverification.connectors.EmailVerificationConnector
import uk.gov.hmrc.emailverification.crypto.Decrypter
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

case class Token(token: String, continueUrl: String)

object Token {
  implicit val tokenReads: Reads[Token] = Json.reads[Token]
}

trait EmailVerificationController extends FrontendController {
  def emailVerificationConnector: EmailVerificationConnector

  def decrypter: Decrypter

  def dateTimeProvider: () => DateTime

  def verify(token: String) = Action.async { implicit request =>
    Future(decrypter.decodeAndDecryptAs[Token](token)).flatMap { decrypted =>
      emailVerificationConnector.verifyEmailAddress(decrypted.token).map(_ => Redirect(decrypted.continueUrl))
    } recover {
      case _ => Redirect(routes.ErrorController.showErrorPage())
    }
  }
}

object EmailVerificationController extends EmailVerificationController {
  override lazy val decrypter: Decrypter = Decrypter
  override val dateTimeProvider = () => DateTime.now()
  override lazy val emailVerificationConnector = EmailVerificationConnector
}
