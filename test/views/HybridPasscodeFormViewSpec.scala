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

package views

import models.{EmailForm, Journey, MessageLabel, MessageLabels}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.gg.test.UnitSpec
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.{HybridPasscodeForm => HybridPasscodeFormView}

class HybridPasscodeFormViewSpec extends UnitSpec with GuiceOneAppPerSuite {

  val view: HybridPasscodeFormView = app.injector.instanceOf[HybridPasscodeFormView]

  "when being rendered in Welsh" when {

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))

    "a Welsh message exists in the labels" should {

      "use the Welsh message from the Journey response" in {

        val renderedView: Html = view(
          EmailForm.form,
          "journeyId",
          RedirectUrl("/redirect"),
          "origin",
          "enterEmailUrl",
          Journey(
            accessibilityStatementUrl = "/a11y",
            deskproServiceName        = "deskproName",
            enterEmailUrl             = None,
            backUrl                   = None,
            serviceTitle              = Some("serviceTitle"),
            emailAddress              = None,
            labels = Some(
              MessageLabels(
                en = MessageLabel(pageTitle = Some("EnglishTitle"), userFacingServiceName = Some("EnglishServiceName")),
                cy = MessageLabel(pageTitle = Some("WelshTitle"), userFacingServiceName = Some("WelshServiceName"))
              )
            )
          )
        )(FakeRequest().withCookies(Cookie("PLAY_LANG", "cy")), messages)

        val document = Jsoup.parse(renderedView.body)
        document.select(".govuk-header__service-name").text() shouldBe "WelshTitle"
      }
    }

    "a Welsh message DOES NOT exists in the labels" when {

      "an English message exists" should {

        "use the English message from the Journey response" in {

          val renderedView: Html = view(
            EmailForm.form,
            "journeyId",
            RedirectUrl("/redirect"),
            "origin",
            "enterEmailUrl",
            Journey(
              accessibilityStatementUrl = "/a11y",
              deskproServiceName        = "deskproName",
              enterEmailUrl             = None,
              backUrl                   = None,
              serviceTitle              = Some("serviceTitle"),
              emailAddress              = None,
              labels = Some(
                MessageLabels(
                  en = MessageLabel(pageTitle = Some("EnglishTitle"), userFacingServiceName = Some("EnglishServiceName")),
                  cy = MessageLabel(pageTitle = None, userFacingServiceName = None)
                )
              )
            )
          )(FakeRequest().withCookies(Cookie("PLAY_LANG", "cy")), messages)

          val document = Jsoup.parse(renderedView.body)
          document.select(".govuk-header__service-name").text() shouldBe "EnglishTitle"
        }
      }

      "an English message does not exist in the labels" should {

        "use the service title from the Journey response" in {

          val renderedView: Html = view(
            EmailForm.form,
            "journeyId",
            RedirectUrl("/redirect"),
            "origin",
            "enterEmailUrl",
            Journey(
              accessibilityStatementUrl = "/a11y",
              deskproServiceName        = "deskproName",
              enterEmailUrl             = None,
              backUrl                   = None,
              serviceTitle              = Some("serviceTitle"),
              emailAddress              = None,
              labels = Some(
                MessageLabels(
                  en = MessageLabel(pageTitle = None, userFacingServiceName = None),
                  cy = MessageLabel(pageTitle = None, userFacingServiceName = None)
                )
              )
            )
          )(FakeRequest().withCookies(Cookie("PLAY_LANG", "cy")), messages)

          val document = Jsoup.parse(renderedView.body)
          document.select(".govuk-header__service-name").text() shouldBe "serviceTitle"
        }
      }
    }
  }

  "when being rendered in English" should {

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

    "if a message exists in the 'labels' from the Journey response" should {

      "return the english message" in {

        val renderedView: Html = view(
          EmailForm.form,
          "journeyId",
          RedirectUrl("/redirect"),
          "origin",
          "enterEmailUrl",
          Journey(
            accessibilityStatementUrl = "/a11y",
            deskproServiceName        = "deskproName",
            enterEmailUrl             = None,
            backUrl                   = None,
            serviceTitle              = Some("serviceTitle"),
            emailAddress              = None,
            labels = Some(
              MessageLabels(
                en = MessageLabel(pageTitle = Some("EnglishTitle"), userFacingServiceName = Some("EnglishServiceName")),
                cy = MessageLabel(pageTitle = Some("WelshTitle"), userFacingServiceName = Some("WelshServiceName"))
              )
            )
          )
        )(FakeRequest().withCookies(Cookie("PLAY_LANG", "en")), messages)

        val document = Jsoup.parse(renderedView.body)
        document.select(".govuk-header__service-name").text() shouldBe "EnglishTitle"
      }
    }

    "if a message DOES NOT exist in the 'labels' from the Journey response" should {

      "return the serviceTitle message" in {

        val renderedView: Html = view(
          EmailForm.form,
          "journeyId",
          RedirectUrl("/redirect"),
          "origin",
          "enterEmailUrl",
          Journey(
            accessibilityStatementUrl = "/a11y",
            deskproServiceName        = "deskproName",
            enterEmailUrl             = None,
            backUrl                   = None,
            serviceTitle              = Some("serviceTitle"),
            emailAddress              = None,
            labels                    = None
          )
        )(FakeRequest().withCookies(Cookie("PLAY_LANG", "en")), messages)

        val document = Jsoup.parse(renderedView.body)
        document.select(".govuk-header__service-name").text() shouldBe "serviceTitle"
      }
    }
  }
}
