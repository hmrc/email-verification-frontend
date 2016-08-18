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
import play.api.mvc.Action
import uk.gov.hmrc.emailverification.crypto.{Decrypter, DecryptionError}
import uk.gov.hmrc.play.frontend.controller.FrontendController

case class Token(email: String, continueUrl: String, expiryTime: String)

trait EmailVerificationController extends FrontendController {

  def decrypter: Decrypter

  def dateTimeProvider: () => DateTime

  private object ExpiredToken {
    def unapply(right: Right[DecryptionError, Token]) = DateTime.parse(right.b.expiryTime).isBefore(dateTimeProvider())
  }
  def verify(encryptedToken: String) = Action { _ =>
    val errorPage = Redirect(routes.ErrorController.showErrorPage())
    decrypter.decryptAs[Token](encryptedToken) match {
      case Left(DecryptionError) => errorPage
      case ExpiredToken() => errorPage
      case Right(token) => Redirect(token.continueUrl)
    }
  }
}

object EmailVerificationController extends EmailVerificationController {
  override lazy val decrypter: Decrypter = Decrypter
  override val dateTimeProvider = () => DateTime.now()
}
