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
import org.jsoup.Jsoup
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment, Mode}
import tools.UnitSpec

class ErrorHandlerSpec extends UnitSpec {
  "showErrorPage" should {
    "display the error page" in new Setup {
      val result = connector.standardErrorTemplate("Error Page Title","heading","message")(request)
      val errorPage = Jsoup.parse(result.body)
      errorPage.title() shouldBe "Error Page Title"
    }
  }

  trait Setup {
    val configuration             = new Configuration(ConfigFactory.load("application.conf"))
    val environment               = new Environment(app.path, app.classloader, Mode.Test)
    val messagesApi: MessagesApi  = app.injector.instanceOf[MessagesApi]

    implicit val appConfig        = new FrontendAppConfig(configuration,environment)
    implicit val request          = FakeRequest()

    val connector                 = new ErrorHandler(messagesApi, configuration)
  }
}
