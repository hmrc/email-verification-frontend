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

package connectors

import config.FrontendAppConfig
import uk.gov.hmrc.gg.test.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class EmailVerificationConnectorSpec extends UnitSpec {

  "verify" should {
    "verify an email address using a token" in new Setup {
      when(mockAppConfig.emailUrl).thenReturn("/email")
      when(mockHttpClient.POST[VerificationToken, Unit](any, eqTo(VerificationToken(token)), eqTo(Nil))(any, any, any, any)).thenReturn(Future.unit)

      connector.verifyEmailAddress(token)(HeaderCarrier())
    }
  }

  trait Setup {
    val token = "some token"
    val mockHttpClient = mock[HttpClient]
    val mockAppConfig = mock[FrontendAppConfig]

    val connector = new EmailVerificationConnector(mockHttpClient, mockAppConfig)(ExecutionContext.global)
  }

}
