/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.emailverification.connectors

import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import tools.MockitoSugarRush
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class EmailVerificationConnectorSpec extends UnitSpec with MockitoSugarRush {

  "verifyEmailAddress" should {
    "verify an email address using a token" in new Setup {
      when(httpMock.POST[VerificationToken, Unit](eqTo(s"$aServiceUrl/email-verification/verified-email-addresses"), eqTo(VerificationToken(token)), eqTo(Nil))(any(), any(), eqTo(headerCarrier), any())).thenReturn(Future.successful {})
      connector.verifyEmailAddress(token)
      verify(httpMock).POST[VerificationToken, Unit](eqTo(s"$aServiceUrl/email-verification/verified-email-addresses"), eqTo(VerificationToken(token)), eqTo(Nil))(any(), any(), eqTo(headerCarrier), any())
      verifyNoMoreInteractions(httpMock)
    }
  }

  trait Setup {
    val token = "some token"
    val httpMock: HttpPost = mock[HttpPost]
    val aServiceUrl = "aServiceUrl"
    implicit val headerCarrier = HeaderCarrier()
    val connector = new EmailVerificationConnector {
      override val http = httpMock
      override val serviceUrl = aServiceUrl
    }
  }

}
