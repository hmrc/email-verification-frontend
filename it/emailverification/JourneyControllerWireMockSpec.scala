/*
 * Copyright 2022 HM Revenue & Customs
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

package emailverification

import org.jsoup.Jsoup
import uk.gov.hmrc.gg.test.WireMockSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

import java.util.UUID
import scala.collection.JavaConverters.asScalaBufferConverter

class JourneyControllerWireMockSpec extends WireMockSpec {

  "GET /journey/:journeyId/email" should {
    "return 200 OK and the email form" in new Setup {
      stubFor(get(s"/email-verification/journey/$journeyId")
        .willReturn(okJson(Json.obj(
          "enterEmailUrl" -> "/enterEmail",
          "accessibilityStatementUrl" -> "/accessibility",
          "deskproServiceName" -> "service-name",
          "backUrl" -> "/back",
          "serviceTitle" -> "Service Name"
        ).toString)))

      val result = await(resourceRequest(s"/email-verification/journey/$journeyId/email")
        .addQueryStringParameters("continueUrl" -> "/some-service")
        .addQueryStringParameters("origin" -> "oranges")
        .get())
      result.status shouldBe OK

      val html = Jsoup.parse(result.body)
      html.title shouldBe "What is your email address?"

      html.selectFirst(".hmrc-header__service-name").text shouldBe "Service Name"
      html.selectFirst(".govuk-back-link").attr("href") shouldBe "/back"
      html.selectFirst(".hmrc-report-technical-issue").attr("href") should endWith ("service=service-name")

      val a11yLink = html.select(".govuk-footer__link").asScala.find(_.text == "Accessibility statement")
      a11yLink shouldBe defined
      a11yLink.value.attr("href") shouldBe "/accessibility"
    }
  }

  "POST /journey/:journeyId/email" when {
    "the email is successfully submitted to the backend" should {
      "return 303 See Other and redirect to the passcode entry page" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/email")
          .withRequestBody(equalToJson(Json.obj("email" -> "aa@bb.cc").toString()))
          .willReturn(okJson(Json.obj("status" -> "accepted").toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/email")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("email" -> Seq("aa@bb.cc"), "continue" -> Seq("")))
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe s"/email-verification/journey/$journeyId/passcode?continueUrl=%2Fsome-service&origin=oranges"
      }
    }

    "the submitted email is invalid" should {
      "return 400 Bad Request and the email form with errors" in new Setup {
        stubFor(get(s"/email-verification/journey/$journeyId")
          .willReturn(okJson(Json.obj(
            "enterEmailUrl" -> "/enterEmail",
            "accessibilityStatementUrl" -> "/accessibility",
            "deskproServiceName" -> "service-name",
            "backUrl" -> "/back",
            "serviceTitle" -> "Service Name"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/email")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("email" -> Seq("not an email"), "continue" -> Seq("")))
        )

        result.status shouldBe BAD_REQUEST
        val html = Jsoup.parse(result.body)
        html.title shouldBe "What is your email address?"
        html.getElementById("email-error").text() shouldBe "Error: Enter an email address in the correct format, like name@example.com"
      }
    }

    "too many emails have been submitted in the session" should {
      "return 403 Forbidden and the Limit Reached page" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/email")
          .withRequestBody(equalToJson(Json.obj("email" -> "aa@bb.cc").toString()))
          .willReturn(forbidden().withBody(Json.obj(
            "status" -> "tooManyAttempts",
            "continueUrl" -> "/continueUrl"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/email")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("email" -> Seq("aa@bb.cc"), "continue" -> Seq("")))
        )

        result.status shouldBe FORBIDDEN

        val html = Jsoup.parse(result.body)
        html.title shouldBe "Max email attempts exceeded"
      }
    }

    "the journey ID is invalid" should {
      "return 404 Not Found" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/email")
          .withRequestBody(equalToJson(Json.obj("email" -> "aa@bb.cc").toString()))
          .willReturn(notFound().withBody(Json.obj(
            "status" -> "journeyNotFound"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/email")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("email" -> Seq("aa@bb.cc"), "continue" -> Seq("")))
        )

        result.status shouldBe NOT_FOUND
      }
    }
  }

  "POST /journey/:journeyId/resend-passcode" when {
    "the passcode email is successfully resent" should {
      "return 303 See Other and redirect to the passcode entry page" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/resend-passcode")
          .willReturn(okJson(Json.obj("status" -> "passcodeResent").toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/resend-passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map.empty[String, Seq[String]])
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe s"/email-verification/journey/$journeyId/passcode?continueUrl=%2Fsome-service&origin=oranges"
      }
    }

    "too many passcodes have been sent to the current email address" should {
      "return 400 Bad Request and display an error" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/resend-passcode")
          .willReturn(okJson(Json.obj(
            "status" -> "tooManyAttemptsForEmail",
            "journey" -> Json.obj(
              "enterEmailUrl" -> "/enterEmail",
              "accessibilityStatementUrl" -> "/accessibility",
              "deskproServiceName" -> "service-name",
              "backUrl" -> "/back",
              "serviceTitle" -> "Service Name",
              "emailAddress" -> "some@email.com"
            )
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/resend-passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map.empty[String, Seq[String]])
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe s"/some-service"
      }
    }

    "too many passcodes have been sent in the current session" should {
      "return 303 See Other and redirect to the continue URL" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/resend-passcode")
          .willReturn(okJson(Json.obj(
            "status" -> "tooManyAttemptsInSession",
            "continueUrl" -> "/some-service"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/resend-passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map.empty[String, Seq[String]])
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe "/some-service"
      }
    }
    
    "the journey ID is invalid" should {
      "return 404 Not Found" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/resend-passcode")
          .willReturn(okJson(Json.obj("status" -> "journeyNotFound").toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/resend-passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map.empty[String, Seq[String]])
        )

        result.status shouldBe NOT_FOUND
      }
    }
    
    "the journey doesn't have an email registered" should {
      "return 500 Internal Server Error" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/resend-passcode")
          .willReturn(okJson(Json.obj("status" -> "noEmailProvided").toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/resend-passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map.empty[String, Seq[String]])
        )

        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
  
  "GET /journey/:journeyId/passcode" when {
    "the journey ID is valid" should {
      "return 200 OK and the passcode entry page" in new Setup {
        stubFor(get(s"/email-verification/journey/$journeyId")
          .willReturn(okJson(Json.obj(
            "enterEmailUrl" -> "/enterEmail",
            "accessibilityStatementUrl" -> "/accessibility",
            "deskproServiceName" -> "service-name",
            "backUrl" -> "/back",
            "serviceTitle" -> "Service Name",
            "emailAddress" -> "some@email.com"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .get()
        )

        result.status shouldBe OK
        val html = Jsoup.parse(result.body)
        html.title shouldBe "Enter the code to confirm the email address"

        html.selectFirst(".hmrc-header__service-name").text shouldBe "Service Name"
        html.selectFirst(".govuk-back-link").attr("href") shouldBe "/back"
        html.selectFirst(".hmrc-report-technical-issue").attr("href") should endWith ("service=service-name")
        html.selectFirst("#email-address").text.trim shouldBe "some@email.com"

        val a11yLink = html.select(".govuk-footer__link").asScala.find(_.text == "Accessibility statement")
        a11yLink shouldBe defined
        a11yLink.value.attr("href") shouldBe "/accessibility"
      }
    }

    "the journey ID is invalid" should {
      "return 404 Not Found" in new Setup {
        stubFor(get(s"/email-verification/journey/$journeyId")
          .willReturn(notFound()))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .get()
        )
        result.status shouldBe NOT_FOUND
      }
    }
  }

  "POST /journey/:journeyId/passcode" when {
    "the passcode is valid" should {
      "return 303 See Other and redirect to the continue URL" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/passcode")
          .willReturn(okJson(Json.obj(
            "status" -> "complete",
            "continueUrl" -> "/some-service"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("passcode" -> Seq("ZZZZZZ")))
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe "/some-service"
      }
    }

    "the passcode is not in a valid format" should {
      "return 400 Bad Request and show the passcode entry page with an error" in new Setup {
        stubFor(get(s"/email-verification/journey/$journeyId")
          .willReturn(okJson(Json.obj(
            "enterEmailUrl" -> "/enterEmail",
            "accessibilityStatementUrl" -> "/accessibility",
            "deskproServiceName" -> "service-name",
            "backUrl" -> "/back",
            "serviceTitle" -> "Service Name"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("passcode" -> Seq("not valid")))
        )

        result.status shouldBe BAD_REQUEST

        val html = Jsoup.parse(result.body)
        html.title shouldBe "Enter the code to confirm the email address"
        html.getElementById("passcode-error").text() shouldBe "Error: Enter a valid security code."
      }
    }
    
    "the passcode is invalid and more attempts are allowed" should {
      "return 400 Bad Request and show the passcode entry page with an error" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/passcode")
          .willReturn(okJson(Json.obj(
            "status" -> "incorrectPasscode",
            "journey" -> Json.obj(
              "enterEmailUrl" -> "/enterEmail",
              "accessibilityStatementUrl" -> "/accessibility",
              "deskproServiceName" -> "service-name",
              "backUrl" -> "/back",
              "serviceTitle" -> "Service Name"
            )
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("passcode" -> Seq("ZZZZZZ")))
        )

        result.status shouldBe BAD_REQUEST

        val html = Jsoup.parse(result.body)
        html.title shouldBe "Enter the code to confirm the email address"
        html.getElementById("passcode-error").text() shouldBe "Error: Enter a valid security code."
      }
    }

    "the passcode is invalid and no more attempts are allowed" should {
      "return 403 Forbidden and show the Limit Reached page" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/passcode")
          .willReturn(okJson(Json.obj(
            "status" -> "tooManyAttempts",
            "continueUrl" -> "/some-service"
          ).toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("passcode" -> Seq("ZZZZZZ")))
        )

        result.status shouldBe SEE_OTHER
        result.header(LOCATION).value shouldBe s"/some-service"
      }
    }

    "the journey ID is invalid" should {
      "return 404 Not Found" in new Setup {
        stubFor(post(s"/email-verification/journey/$journeyId/passcode")
          .willReturn(okJson(Json.obj("status" -> "journeyNotFound").toString)))

        val result = await(resourceRequest(s"/email-verification/journey/$journeyId/passcode")
          .addQueryStringParameters("continueUrl" -> "/some-service")
          .addQueryStringParameters("origin" -> "oranges")
          .withFollowRedirects(false)
          .post(Map("passcode" -> Seq("ZZZZZZ")))
        )

        result.status shouldBe NOT_FOUND
      }
    }
  }

  trait Setup {
    val journeyId = UUID.randomUUID().toString
  }
}
