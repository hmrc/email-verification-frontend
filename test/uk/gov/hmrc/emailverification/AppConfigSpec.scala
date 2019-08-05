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

package uk.gov.hmrc.emailverification

import com.typesafe.config.ConfigFactory
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment, Mode}

import scala.concurrent.Future

class AppConfigSpec extends MockitoSugar with GuiceOneAppPerSuite with Matchers with WordSpecLike {

  val configuration = new Configuration(ConfigFactory.load("application.conf"))
  val environment   = new Environment(app.path, app.classloader, Mode.Test)
  val appConfig     = new FrontendAppConfig(configuration,environment)

  "analyticsToken" should {
    "be read as it is from configuration" in {
      val analyticsTokenValue = "some-analytics-token"
      val appConfig     = new FrontendAppConfig(configuration ++ Configuration(s"${environment.mode}.google-analytics.token" -> analyticsTokenValue),environment)
      appConfig.analyticsToken shouldBe analyticsTokenValue
    }
    "throw exception if not configured" in {
      val configuration = mock[Configuration]
      val appConfig     = new FrontendAppConfig(configuration,environment)
      when(configuration.getString(s"${environment.mode}.google-analytics.token")) thenReturn Future.successful(None)
      val exception = intercept[Exception](appConfig.analyticsToken)
      exception.getMessage shouldBe s"Missing configuration key: ${environment.mode}.google-analytics.token"
    }
  }

  "analyticsHost" should {
    "be read as it is from configuration" in {
      val analyticsHostValue = "some-analytics-host"
      val appConfig     = new FrontendAppConfig(configuration ++ Configuration(s"${environment.mode}.google-analytics.host" -> analyticsHostValue),environment)
      appConfig.analyticsHost shouldBe analyticsHostValue
    }
    "throw exception if not configured" in {
      val configuration = mock[Configuration]
      val appConfig     = new FrontendAppConfig(configuration,environment)
      when(configuration.getString(s"${environment.mode}.google-analytics.host")) thenReturn Future.successful(None)
      val exception = intercept[Exception](appConfig.analyticsHost)
      exception.getMessage shouldBe s"Missing configuration key: ${environment.mode}.google-analytics.host"
    }
  }

  "reportAProblemPartialUrl" should {
    "return relative problem reporting url for js" in {
      appConfig.reportAProblemPartialUrl shouldBe "/contact/problem_reports_ajax?service=email-verification-frontend"
    }
  }

  "reportAProblemNonJSUrl" should {
    "return relative problem reporting url for non js" in {
      appConfig.reportAProblemNonJSUrl shouldBe "/contact/problem_reports_nonjs?service=email-verification-frontend"
    }
  }

}
