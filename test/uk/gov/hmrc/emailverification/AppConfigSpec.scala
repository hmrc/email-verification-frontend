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

package uk.gov.hmrc.emailverification

import play.api.Configuration
import uk.gov.hmrc.play.test.UnitSpec

class AppConfigSpec extends UnitSpec {

  "analyticsToken" should {
    "be read as it is from configuration" in new Setup {
      val analyticsTokenValue = "some-analytics-token"
      override val testConfigurationMap = Map(s"$testEnv.google-analytics.token" -> analyticsTokenValue)
      appConfig.analyticsToken shouldBe analyticsTokenValue
    }
    "throw exception if not configured" in new Setup {
      val exception = intercept[Exception](appConfig.analyticsToken)
      exception.getMessage shouldBe s"Missing configuration key: $testEnv.google-analytics.token"
    }
  }

  "analyticsHost" should {
    "be read as it is from configuration" in new Setup {
      val analyticsHostValue = "some-analytics-host"
      override val testConfigurationMap = Map(s"$testEnv.google-analytics.host" -> analyticsHostValue)
      appConfig.analyticsHost shouldBe analyticsHostValue
    }
    "throw exception if not configured" in new Setup {
      val exception = intercept[Exception](appConfig.analyticsHost)
      exception.getMessage shouldBe s"Missing configuration key: $testEnv.google-analytics.host"
    }
  }

  "reportAProblemPartialUrl" should {
    "return relative problem reporting url for js" in new Setup {
      appConfig.reportAProblemPartialUrl shouldBe "/contact/problem_reports_ajax?service=email-verification-frontend"
    }
  }

  "reportAProblemNonJSUrl" should {
    "return relative problem reporting url for non js" in new Setup {
      appConfig.reportAProblemNonJSUrl shouldBe "/contact/problem_reports_nonjs?service=email-verification-frontend"
    }
  }

  trait Setup {

    val testConfigurationMap = Map.empty[String,Any]
    val testEnv = "Test"

    lazy val appConfig = new AppConfig {
      override protected val configuration = Configuration.from(testConfigurationMap)
      override protected val env = testEnv
    }
  }

}
