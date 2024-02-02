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
import org.apache.commons.codec.binary.Base64.encodeBase64String
import play.api.mvc.Result
import play.api.{Environment, Mode}
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.Crypted
import uk.gov.hmrc.gg.test.UnitSpec
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, PermitAllOnDev, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.tools.Stubs

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class EmailVerificationControllerSpec extends UnitSpec {

  "verify" should {
    "redirect to continue url if link is verified" in new Setup {
      when(mockDecrypter.decryptAs[Token](Crypted(encryptedToken))).thenReturn(Success(Token(token, continueUrl)))
      when(mockEmailVerificationConnector.verifyEmailAddress(eqTo(token))(any)).thenReturn(Future.unit)
      val result: Future[Result] = controller.verify(encryptedAndEncodedToken)(FakeRequest())

      status(result)         shouldBe 303
      redirectLocation(result) should contain(continueUrl)
    }

    "redirect to error page if link is not verified" in new Setup {
      when(mockDecrypter.decryptAs[Token](Crypted(encryptedToken))).thenReturn(Success(Token(token, continueUrl)))
      when(mockEmailVerificationConnector.verifyEmailAddress(eqTo(token))(any)).thenReturn(Future.failed(new RuntimeException))
      val result: Future[Result] = controller.verify(encryptedAndEncodedToken)(FakeRequest())

      status(result)         shouldBe 303
      redirectLocation(result) should contain("/error")
    }
  }

  trait Setup {
    val continueUrl = "/continue"
    val encryptedToken = "some-encrypted-string"
    val encryptedAndEncodedToken: String = encodeBase64String(encryptedToken.getBytes("UTF-8"))
    val token = "some token"
    val mockDecrypter: Decrypter = mock[Decrypter]
    val mockEmailVerificationConnector: EmailVerificationConnector = mock[EmailVerificationConnector]
    val controller = new EmailVerificationController(mockEmailVerificationConnector, mockDecrypter, Stubs.stubMessagesControllerComponents())(
      ExecutionContext.global
    )
  }
}
