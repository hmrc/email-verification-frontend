/*
 * Copyright 2019 HM Revenue & Customs
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

import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment, Mode}
import tools.MockitoSugarRush
import uk.gov.hmrc.emailverification.FrontendAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class EmailVerificationConnectorSpec extends UnitSpec with MockitoSugarRush with GuiceOneAppPerSuite {

  "verifyEmailAddress" should {
    "verify an email address using a token" in new Setup {
      when(httpMock.POST[VerificationToken, Unit](any(), eqTo(VerificationToken(token)), eqTo(Nil))(any(), any(), eqTo(headerCarrier), any())).thenReturn(Future.successful {})
      connector.verifyEmailAddress(token)(headerCarrier,ec)
      verify(httpMock).POST[VerificationToken, Unit](any(), eqTo(VerificationToken(token)), eqTo(Nil))(any(), any(), eqTo(headerCarrier), any())
      verifyNoMoreInteractions(httpMock)
    }
  }

  trait Setup {
    val token = "some token"
    val httpMock: HttpClient = mock[HttpClient]
    val configuration = new Configuration(ConfigFactory.load("application.conf"))
    val environment   = new Environment(app.path, app.classloader, Mode.Test)
    val appConfig     = new FrontendAppConfig(configuration,environment)

    implicit val headerCarrier = HeaderCarrier()
    implicit val ec = app.injector.instanceOf[ExecutionContext]
    val connector = new EmailVerificationConnector(httpMock,appConfig)
  }

}
