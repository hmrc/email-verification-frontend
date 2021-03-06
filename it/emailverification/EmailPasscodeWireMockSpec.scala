/*
 * Copyright 2021 HM Revenue & Customs
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

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.jsoup.Jsoup
import org.scalatest.Assertion
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.HeaderNames
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Injecting
import uk.gov.hmrc.gg.test.WireMockSpec
import uk.gov.hmrc.play.it.SessionCookieEncryptionSupport

import scala.collection.JavaConverters._

class EmailPasscodeWireMockSpec extends WireMockSpec with Injecting with SessionCookieEncryptionSupport with TableDrivenPropertyChecks {

  val messagesEn = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))
  val messagesCy = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))

  "get /emailform" should {
    "show enter your email page" in new Setup {
      val response = await(
        resourceRequest("/email-verification/emailform")
          .addQueryStringParameters("continue" -> continueUrl)
          .withSession("sessionId" -> newSessionId)
          .withFollowRedirects(false)
          .get()
      )
      response.status shouldBe OK
      val html = Jsoup.parse(response.body)
      html.title shouldBe messagesEn("emailform.title")
    }
  }

  "get /emailform with invalid continue url" should {
    "return bad request error" in new Setup {
      val response = await(
        resourceRequest("/email-verification/emailform")
          .addQueryStringParameters("continue" -> "IAmNotAContinueUrlAndImOkWithThat")
          .withSession("sessionId" -> newSessionId)
          .withFollowRedirects(false)
          .get()
      )
      response.status shouldBe BAD_REQUEST
    }
  }

  "post /emailform" should {
    "request a passcode email and show passcode entry form" in new Setup {
      expectRequestPasscodeToReturn(201)

      val response = await(
        resourceRequest("/email-verification/emailform")
          .withSession("sessionId" -> newSessionId)
          .withFollowRedirects(false)
          .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress)))
      )
      response.status shouldBe OK
      val html = Jsoup.parse(response.body)
      html.title shouldBe messagesEn("passcodeform.title")
      verifyLanguageCookie("en")
    }

    "request a passcode email with the welsh language cookie" in new Setup {
      expectRequestPasscodeToReturn(201)

      val response = await(
        resourceRequest("/email-verification/emailform")
          .withSession("sessionId" -> newSessionId)
          .withCookies(DefaultWSCookie("PLAY_LANG", "cy"))
          .withFollowRedirects(false)
          .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress), "lang" -> Seq("cy")))
      )
      response.status shouldBe OK
      val html = Jsoup.parse(response.body)
      html.title shouldBe messagesCy("passcodeform.title")
      verifyLanguageCookie("cy")
    }
  }

  "post /emailform with invalid email address" should {
    "return bad request error" in new Setup {
      def response = await(resourceRequest("/email-verification/emailform")
        .withSession("sessionId" -> newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(continueUrl), "email" -> Seq("IAmNotAValidEmailAddress")))
      )

      response.status shouldBe BAD_REQUEST
      val html = Jsoup.parse(response.body)
      html.getElementsByAttributeValue("href", "#email").first().text() shouldBe messagesEn("emailform.error.invalidEmailFormat")
    }
  }

  "post /emailform when email-verification/request-passcode returns 403 (forbidden due to too many email attempts)" should {
    "redirect to max emails limit reached error page" in new Setup {
      expectRequestPasscodeToReturn(403)

      def response = await(resourceRequest("/email-verification/emailform")
        .withSession("sessionId" -> newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress)))
      )

      response.status shouldBe 303
      response.header(HeaderNames.LOCATION).get should include("/emailLimitReached")
    }
  }

  "post /emailform when email-verification/request-passcode returns 409 (already verified)" should {
    "redirect to email already verified page" in new Setup {
      expectRequestPasscodeToReturn(409)

      def response = await(resourceRequest("/email-verification/emailform")
        .withSession("sessionId" -> newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress)))
      )

      response.status shouldBe 303
      response.header(HeaderNames.LOCATION).get should include("/emailAlreadyVerified")
    }
  }

  "post /emailform when email-verification/request-passcode returns 401 (no sessionId)" should {
    "return 401 unauthorised response page" in new Setup {
      expectRequestPasscodeToReturn(401)

      def response = await(resourceRequest("/email-verification/emailform")
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress)))
      )

      response.status shouldBe 401
      val html = Jsoup.parse(response.body)
      html.title shouldBe messagesEn("global.error.InternalServerError500.title")
    }
  }

  "post /passcodeform with correct passcode (email-verification/verify-passcode 201)" should {
    "redirect to success page" in new Setup {
      expectVerifyPasscodeToReturn(201)

      def response = await(resourceRequest("/email-verification/passcodeform")
        .withSession("sessionId" -> newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(continueUrl), "email" -> Seq(newEmailAddress), "passcode" -> Seq(correctPasscode)))
      )

      response.status shouldBe 303
      response.header(HeaderNames.LOCATION).get should include("/success")
    }
  }

  "post /passcodeform with invalid passcode formats (should be 6 upper case alphabet characters, no vowels)" should {

    forAll(Table(
      ("invalidPasscode"),
      Setup.passcodeWithVowels,
      Setup.passcodeTooLong,
      Setup.emptyPasscode
    )) { case (invalidPasscode) =>
      s"return 400 and error on passcode page when submitting invalid passcode: $invalidPasscode" in {
        def response = await(resourceRequest("/email-verification/passcodeform")
          .withSession("sessionId" -> Setup.newSessionId)
          .withFollowRedirects(false)
          .post(Map("continue" -> Seq(Setup.continueUrl), "email" -> Seq(Setup.newEmailAddress), "passcode" -> Seq(invalidPasscode)))
        )

        response.status shouldBe 400
        val html = Jsoup.parse(response.body)
        html.getElementsByAttributeValue("href", "#passcode").first().text() shouldBe messagesEn("passcodeform.error.invalidFormat")
      }
    }
  }


  "post /passcodeform with incorrect passcode (email-verification/verify-passcode 404)" should {
    "show passcode form again with error message" in new Setup {
      expectVerifyPasscodeToReturn(404)

      def response = await(resourceRequest("/email-verification/passcodeform")
        .withSession("sessionId" -> Setup.newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(Setup.continueUrl), "email" -> Seq(Setup.newEmailAddress), "passcode" -> Seq(correctPasscode)))
      )

      response.status shouldBe 400
      val html = Jsoup.parse(response.body)
      html.getElementsByAttributeValue("href", "#passcode").first().text() shouldBe messagesEn("passcodeform.error.invalidFormat")
    }
  }

  "post /passcodeform with incorrect passcode too many times (email-verification/verify-passcode 403)" should {
    "show passcode attempt limit reached error page" in new Setup {
      expectVerifyPasscodeToReturn(403)

      def response = await(resourceRequest("/email-verification/passcodeform")
        .withSession("sessionId" -> Setup.newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(Setup.continueUrl), "email" -> Seq(Setup.newEmailAddress), "passcode" -> Seq(correctPasscode)))
      )

      response.status shouldBe 303
      response.header(HeaderNames.LOCATION).get should include("/passcodeLimitReached")
    }
  }

  "post /language/cymraeg/passcodeForm with email address field" should {
    "show passcode form in welsh while correctly showing the email address" in new Setup {
      def response = await(resourceRequest("/email-verification/language/cymraeg/passcodeForm")
        .withSession("sessionId" -> Setup.newSessionId)
        .withFollowRedirects(false)
        .post(Map("continue" -> Seq(Setup.continueUrl), "email" -> Seq("some.email@address.com"), "passcode" -> Seq("")))
      )

      response.status shouldBe 200
      val html = Jsoup.parse(response.body)
      val langSelectorDiv = html.getElementsByClass("lang-selector").first()
      langSelectorDiv.getElementsByAttributeValue("type", "submit").first().attr("value") shouldBe "English"
      html.getElementById("email-address").text.trim shouldBe messagesEn("some.email@address.com")
    }
  }

  trait Setup extends Wiremocks {

    def newSessionId = "sessionId-" + UUID.randomUUID().toString

    val continueUrl = "/some/calling/service"

    def newEmailAddress = UUID.randomUUID().toString + "@email.com"

    val correctPasscode = "KDFGHY"
    val passcodeWithVowels = "AAEEII"
    val emptyPasscode = ""
    val passcodeTooLong = "GHYFRWC"
  }

  object Setup extends Setup

  trait Wiremocks {

    def expectRequestPasscodeToReturn(status: Int) = stubFor(
      post(
        urlEqualTo("/email-verification/request-passcode"))
        .willReturn(aResponse().withStatus(status))
    )

    def expectVerifyPasscodeToReturn(status: Int) = stubFor(
      post(
        urlEqualTo("/email-verification/verify-passcode"))
        .willReturn(aResponse().withStatus(status))
    )

    private def requestPasscodeRequestPayload: JsValue = {
      val emails = WireMock.findAll(postRequestedFor(urlEqualTo("/email-verification/request-passcode"))).asScala
      val emailSendRequest = emails.last.getBodyAsString
      Json.parse(emailSendRequest)
    }

    def verifyLanguageCookie(lang: String): Assertion = {
      val email = requestPasscodeRequestPayload
      (email \ "lang").as[String] shouldBe lang
    }

  }

}

