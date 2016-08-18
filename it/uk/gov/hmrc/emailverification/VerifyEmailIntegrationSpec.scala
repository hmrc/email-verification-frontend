package uk.gov.hmrc.emailverification

import java.util.UUID

import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.test.FakeApplication
import uk.gov.hmrc.crypto.ApplicationCrypto._
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.emailverification.stubs.EmailVerificationStubs.{stubCreateVerifiedEmail, verifyCreateVerifiedEmail}

class VerifyEmailIntegrationSpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = Map(
    "microservice.services.email-verification.host" -> WiremockHelper.wiremockHost,
    "microservice.services.email-verification.port" -> WiremockHelper.wiremockPort
  ))

  feature("verify email") {

    val continueUrl = "/continue-url"

    def jsonToken(token: String) =
      s"""
         |{
         | "token": "$token",
         | "continueUrl": "$continueUrl"
         |}
        """.stripMargin
    def encrypt(value: String) = QueryParameterCrypto.encrypt(PlainText(value)).value

    scenario("link is verified") {
      Given("an encrypted payload containing a token and a continue url")
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encrypt(jsonToken(token))

      stubCreateVerifiedEmail(token, 201)

      When("call GET on verify url passing the encrypted payload as token")
      val response = client("/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the continue url")
      response.header(HeaderNames.LOCATION) should contain("/continue-url")

      verifyCreateVerifiedEmail()
    }

    scenario("link is not verified") {
      Given("an encrypted payload containing an invalid token and continue url")
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encrypt(jsonToken(token))
      stubCreateVerifiedEmail(token, 400)

      When("call GET on verify url passing the encrypted payload as token")
      val response = client("/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the error url")
      response.header(HeaderNames.LOCATION) should contain("/email-verification/error")
    }
  }


  private def client(path: String) = WS.url(s"http://localhost:$port/email-verification$path").withFollowRedirects(false)

}
