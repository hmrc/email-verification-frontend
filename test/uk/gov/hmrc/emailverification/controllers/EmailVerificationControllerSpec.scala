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
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import tools.MockitoSugarRush
import uk.gov.hmrc.emailverification.crypto.Decrypter
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class EmailVerificationControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures with IntegrationPatience with MockitoSugarRush {

  "verify" should {
    "redirect to continue url if expiry time is now" in new Setup {

      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Token(email, continueUrl, currentTime))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(continueUrl)
    }

    "redirect to continue url if expiry time is in future" in new Setup {
      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Token(email, continueUrl, currentTime.plusDays(1)))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(continueUrl)
    }

    "redirect to error page if expiry time is in past" in new Setup {
      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Token(email, continueUrl, currentTime.minusDays(1)))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(errorUrl)
    }
  }

  trait Setup {
    val currentTime = DateTime.now
    val email = "john@doe.com"
    val continueUrl = "/continue"
    val errorUrl = "/email-verification/error"
    val encryptedToken = "some-encrypted-string"
    val decrypterMock: Decrypter = mock[Decrypter]
    val controller = new EmailVerificationController {
      override val decrypter = decrypterMock
      override lazy val dateTimeProvider = () => currentTime
    }
    implicit val request = FakeRequest()
  }


}
