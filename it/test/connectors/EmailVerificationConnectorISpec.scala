/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import models.ValidatePasscodeResponse.Complete
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import support.IntegrationBaseSpec

class EmailVerificationConnectorISpec extends IntegrationBaseSpec with ScalaFutures with IntegrationPatience {

  class Test {
    lazy val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    lazy val connector: EmailVerificationConnector = app.injector.instanceOf[EmailVerificationConnector]
  }

  "EmailVerificationConnector" should {

    ".verifyEmailAddress" should {
      val token:             String = "test-token"
      val verificationToken: VerificationToken = VerificationToken(token)

      "return Unit when the email address is verified successfully" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verified-email-addresses"))
            .withRequestBody(
              equalToJson(Json.toJson(verificationToken).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(201)
            )
        )

        val result: Unit = connector.verifyEmailAddress(token).futureValue
        result shouldBe ()
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verified-email-addresses"))
            .withRequestBody(
              equalToJson(Json.toJson(verificationToken).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.verifyEmailAddress(token).futureValue
        }
        exception.getMessage should include("Internal Server Error")
      }
    }

    ".requestPasscode" should {
      val email:           String = "testemail@email.com"
      val lang:            String = "en"
      val passcodeRequest: PasscodeRequest = PasscodeRequest(email, "email-verification-frontend", lang)

      "return Unit when the email passcode is verified successfully" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/request-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(201)
            )
        )

        val result: Unit = connector.requestPasscode(email, lang).futureValue
        result shouldBe ()
      }

      "throw an exception when the email verification service returns a 401" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/request-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(401)
                .withBody("Missing session ID")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.requestPasscode(email, lang).futureValue
        }
        exception.getMessage should include("Missing session ID")
      }

      "throw an exception when the email verification service returns a 403" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/request-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(403)
                .withBody("Max new emails exceeded")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.requestPasscode(email, lang).futureValue
        }
        exception.getMessage should include("Max new emails exceeded")
      }

      "throw an exception when the email verification service returns a 409" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/request-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(409)
                .withBody("Email already verified")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.requestPasscode(email, lang).futureValue
        }
        exception.getMessage should include("Email already verified")
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/request-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.requestPasscode(email, lang).futureValue
        }
        exception.getMessage should include("Internal Server Error")
      }
    }

    ".verifyPasscode" should {
      val email:           String = "testemail@email.com"
      val passCode:        String = "123456"
      val passcodeRequest: PasscodeVerificationRequest = PasscodeVerificationRequest(email, passCode)

      "return Unit when the email passcode is verified successfully and returns a 201" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(201)
            )
        )

        val result: Unit = connector.verifyPasscode(email, passCode).futureValue
        result shouldBe ()
      }

      "return Unit when the email passcode is verified successfully and returns a 204" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val result: Unit = connector.verifyPasscode(email, passCode).futureValue
        result shouldBe ()
      }

      "throw an exception when the email verification service returns a 401" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(401)
                .withBody("Missing session ID")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.verifyPasscode(email, passCode).futureValue
        }
        exception.getMessage should include("Missing session ID")
      }

      "throw an exception when the email verification service returns a 403" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(403)
                .withBody("Max passcode attempts exceeded")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.verifyPasscode(email, passCode).futureValue
        }
        exception.getMessage should include("Max passcode attempts exceeded")
      }

      "throw an exception when the email verification service returns a 404" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Incorrect passcode")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.verifyPasscode(email, passCode).futureValue
        }
        exception.getMessage should include("Incorrect passcode")
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo("/email-verification/verify-passcode"))
            .withRequestBody(
              equalToJson(Json.toJson(passcodeRequest).toString())
            )
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.verifyPasscode(email, passCode).futureValue
        }
        exception.getMessage should include("Internal Server Error")
      }

    }

    ".submitEmail" should {
      val journeyId: String = "test-journey-id"
      val email:     String = "testemail@mail.com"
      val requestBody = Json.obj("email" -> email)

      "return SubmitEmailResponse when the email is accepted" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/email"))
            .withRequestBody(equalToJson(requestBody.toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "accepted").toString())
            )
        )

        val result: SubmitEmailResponse = connector.submitEmail(journeyId, email).futureValue
        result shouldBe SubmitEmailResponse.Accepted
      }

      "return SubmitEmailResponse.JourneyNotFound when the service indicates the journey was not found" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/email"))
            .withRequestBody(equalToJson(requestBody.toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "journeyNotFound").toString())
            )
        )

        val result: SubmitEmailResponse = connector.submitEmail(journeyId, email).futureValue
        result shouldBe SubmitEmailResponse.JourneyNotFound
      }

      "return SubmitEmailResponse with TooManyAttempts when the service indicates too many attempts" in new Test {
        val continueUrl: String = "http://example.com/continue"
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/email"))
            .withRequestBody(equalToJson(requestBody.toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "tooManyAttempts", "continueUrl" -> continueUrl).toString())
            )
        )

        val result: SubmitEmailResponse = connector.submitEmail(journeyId, email).futureValue
        result shouldBe SubmitEmailResponse.TooManyAttempts(continueUrl)
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/email"))
            .withRequestBody(equalToJson(requestBody.toString()))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.submitEmail(journeyId, email).futureValue
        }
        exception.getMessage should include("Internal Server Error")
      }
    }

    ".resendPasscode" should {
      val journeyId: String = "test-journey-id"

      "return ResendPasscodeResponse when the passcode is resent" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "passcodeResent").toString())
            )
        )
        val result: ResendPasscodeResponse = connector.resendPasscode(journeyId).futureValue
        result shouldBe ResendPasscodeResponse.PasscodeResent
      }

      "return ResendPasscodeResponse.JourneyNotFound when the service indicates the journey was not found" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "journeyNotFound").toString())
            )
        )

        val result: ResendPasscodeResponse = connector.resendPasscode(journeyId).futureValue
        result shouldBe ResendPasscodeResponse.JourneyNotFound
      }

      "return ResendPasscodeResponse.NoEmailProvided when the service indicates no email was provided" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "noEmailProvided").toString())
            )
        )

        val result: ResendPasscodeResponse = connector.resendPasscode(journeyId).futureValue
        result shouldBe ResendPasscodeResponse.NoEmailProvided
      }

      "return ResendPasscodeResponse with TooManyAttemptsInSession when the service indicates too many attempts in session" in new Test {
        val continueUrl: String = "http://example.com/continue"
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "tooManyAttemptsInSession", "continueUrl" -> continueUrl).toString())
            )
        )

        val result: ResendPasscodeResponse = connector.resendPasscode(journeyId).futureValue
        result shouldBe ResendPasscodeResponse.TooManyAttemptsInSession(continueUrl)
      }

      "return ResendPasscodeResponse with TooManyAttemptsForEmail when the service indicates too many attempts for email" in new Test {
        val expectedJourney: Journey = Journey(
          accessibilityStatementUrl = "http://example.com/accessibility",
          deskproServiceName        = "Test Service",
          enterEmailUrl             = Some("http://example.com/enter-email"),
          backUrl                   = Some("http://example.com/back"),
          serviceTitle              = Some("Test Service Title"),
          emailAddress              = Some("testemail@email.com"),
          labels                    = None
        )

        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "tooManyAttemptsForEmail", "journey" -> Json.toJson(expectedJourney)).toString())
            )
        )

        val result: ResendPasscodeResponse = connector.resendPasscode(journeyId).futureValue
        result shouldBe ResendPasscodeResponse.TooManyAttemptsForEmail(expectedJourney)
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/resend-passcode"))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.resendPasscode(journeyId).futureValue
        }
      }
    }

    ".validatePasscode" should {
      val journeyId:   String = "test-journey-id"
      val passcode:    String = "123456"
      val continueUrl: String = "http://example.com/continue"

      "return ValidatePasscodeResponse when the passcode is complete" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/passcode"))
            .withRequestBody(equalToJson(Json.obj("passcode" -> passcode).toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "complete", "continueUrl" -> continueUrl).toString())
            )
        )

        val result: ValidatePasscodeResponse = connector.validatePasscode(journeyId, passcode).futureValue
        result shouldBe Complete(continueUrl)
      }

      "return ValidatePasscodeResponse.IncorrectPasscode when the passcode is incorrect" in new Test {
        val expectedJourney: Journey = Journey(
          accessibilityStatementUrl = "http://example.com/accessibility",
          deskproServiceName        = "Test Service",
          enterEmailUrl             = Some("http://example.com/enter-email"),
          backUrl                   = Some("http://example.com/back"),
          serviceTitle              = Some("Test Service Title"),
          emailAddress              = Some("testemail@email.com"),
          labels                    = None
        )

        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/passcode"))
            .withRequestBody(equalToJson(Json.obj("passcode" -> passcode).toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "incorrectPasscode", "journey" -> Json.toJson(expectedJourney)).toString())
            )
        )

        val result: ValidatePasscodeResponse = connector.validatePasscode(journeyId, passcode).futureValue
        result shouldBe ValidatePasscodeResponse.IncorrectPasscode(expectedJourney)
      }

      "return ValidatePasscodeResponse.TooManyAttempts when the service indicates too many attempts" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/passcode"))
            .withRequestBody(equalToJson(Json.obj("passcode" -> passcode).toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "tooManyAttempts", "continueUrl" -> continueUrl).toString())
            )
        )

        val result: ValidatePasscodeResponse = connector.validatePasscode(journeyId, passcode).futureValue
        result shouldBe ValidatePasscodeResponse.TooManyAttempts(continueUrl)
      }

      "return ValidatePasscodeResponse.JourneyNotFound when the service indicates the journey was not found" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/passcode"))
            .withRequestBody(equalToJson(Json.obj("passcode" -> passcode).toString()))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.obj("status" -> "journeyNotFound").toString())
            )
        )

        val result: ValidatePasscodeResponse = connector.validatePasscode(journeyId, passcode).futureValue
        result shouldBe ValidatePasscodeResponse.JourneyNotFound
      }

      "throw an exception when the email verification service returns an error" in new Test {
        stubFor(
          post(urlEqualTo(s"/email-verification/journey/$journeyId/passcode"))
            .withRequestBody(equalToJson(Json.obj("passcode" -> passcode).toString()))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val exception: Exception = intercept[Exception] {
          connector.validatePasscode(journeyId, passcode).futureValue
        }
        exception.getMessage should include("Internal Server Error")
      }

    }

    ".getJourney" should {
      val journeyId: String = "test-journey-id"

      "return Some(Journey) when the journey is found" in new Test {
        val expectedJourney: Journey = Journey(
          accessibilityStatementUrl = "http://example.com/accessibility",
          deskproServiceName        = "Test Service",
          enterEmailUrl             = Some("http://example.com/enter-email"),
          backUrl                   = Some("http://example.com/back"),
          serviceTitle              = Some("Test Service Title"),
          emailAddress              = Some("testemail@email.com"),
          labels                    = None
        )

        stubFor(
          get(urlEqualTo(s"/email-verification/journey/$journeyId"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.toJson(expectedJourney).toString())
            )
        )

        val result: Option[Journey] = connector.getJourney(journeyId).futureValue
        result shouldBe Some(expectedJourney)
      }

      "return None when the journey is not found" in new Test {
        stubFor(
          get(urlEqualTo(s"/email-verification/journey/$journeyId"))
            .willReturn(
              aResponse()
                .withStatus(404)
            )
        )

        val result: Option[Journey] = connector.getJourney(journeyId).futureValue
        result shouldBe None
      }
    }
  }
}
