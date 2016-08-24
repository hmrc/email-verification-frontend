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
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import tools.MockitoSugarRush
import uk.gov.hmrc.emailverification.connectors.EmailVerificationConnector
import uk.gov.hmrc.emailverification.crypto.Decrypter
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class EmailVerificationControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures with IntegrationPatience with MockitoSugarRush {

  "verify" should {
    "redirect to continue url if link is verified" in new Setup {

      when(decrypterMock.decodeAndDecryptAs[Token](encryptedToken)).thenReturn(Token(token, continueUrl))
      when(emailVerificationConnectorMock.verifyEmailAddress(eqTo(token))(any[HeaderCarrier])).thenReturn(Future.successful {})
      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(continueUrl)
      verify(decrypterMock).decodeAndDecryptAs[Token](encryptedToken)
      verify(emailVerificationConnectorMock).verifyEmailAddress(eqTo(token))(any[HeaderCarrier])
      verifyNoMoreInteractions(decrypterMock, emailVerificationConnectorMock)
    }

    "redirect to error page if link is not verified" in new Setup {
      when(decrypterMock.decodeAndDecryptAs[Token](encryptedToken)).thenReturn(Token(token, continueUrl))
      when(emailVerificationConnectorMock.verifyEmailAddress(eqTo(token))(any[HeaderCarrier])).thenReturn(Future.failed(new RuntimeException))
      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(errorUrl)
      verify(decrypterMock).decodeAndDecryptAs[Token](encryptedToken)
      verify(emailVerificationConnectorMock).verifyEmailAddress(eqTo(token))(any[HeaderCarrier])
      verifyNoMoreInteractions(decrypterMock, emailVerificationConnectorMock)
    }
  }

  trait Setup {
    val currentTime = DateTime.now
    val email = "john@doe.com"
    val continueUrl = "/continue"
    val errorUrl = "/email-verification/error"
    val encryptedToken = "some-encrypted-string"
    val token = "some token"
    val decrypterMock: Decrypter = mock[Decrypter]
    val emailVerificationConnectorMock: EmailVerificationConnector = mock[EmailVerificationConnector]
    val controller = new EmailVerificationController {
      override val decrypter = decrypterMock
      override lazy val dateTimeProvider = () => currentTime
      override lazy val emailVerificationConnector = emailVerificationConnectorMock
    }
    implicit val request = FakeRequest()
  }


}
