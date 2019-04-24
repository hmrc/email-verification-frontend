package uk.gov.hmrc.emailverification

import java.util.UUID

import javax.inject.Inject
import org.jsoup.Jsoup
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import play.api.Play
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import play.api.test.FakeApplication
//import uk.gov.hmrc.crypto.ApplicationCrypto._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.emailverification.stubs.EmailVerificationStubs.{stubCreateVerifiedEmail, verifyCreateVerifiedEmail}

import scala.collection.JavaConverters._

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
    def encryptAndEncode(value: String) = new String(new ApplicationCrypto(Play.current.configuration.underlying).QueryParameterCrypto.encrypt(PlainText(value)).toBase64)

    scenario("link is verified") {
      Given("an encrypted payload containing a token and a continue url")
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encryptAndEncode(jsonToken(token))

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
      val encryptedJsonToken = encryptAndEncode(jsonToken(token))
      stubCreateVerifiedEmail(token, 400)

      When("call GET on verify url passing the encrypted payload as token")
      val response = client("/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the error url")
      response.header(HeaderNames.LOCATION) should contain("/email-verification/error")
    }

    val invalidTokenScenarios = Table(
      ("scenarioName", "encryptedJsonToken"),
      ("invalid json", () => encryptAndEncode("this is an invalid json")),
      ("unexpected json", () => encryptAndEncode( """{"unexpectedField":"some-value"}""")),
      ("invalid encrypted payload", () => "asdfghjkl")
    )

    forAll(invalidTokenScenarios) { (scenarioName: String, encryptedJsonToken: () => String) =>

      scenario(scenarioName) {
        Given(s"an encrypted payload containing a $scenarioName")
        val token = UUID.randomUUID().toString
        stubCreateVerifiedEmail(token, 201)

        When("call GET on verify url passing the encrypted payload as token")
        val response = client("/verify").withQueryString("token" -> encryptedJsonToken()).get().futureValue

        Then("response status should be 303 redirect")
        response.status shouldBe 303

        And("response Location header should be the error url")
        response.header(HeaderNames.LOCATION) should contain("/email-verification/error")
      }
    }

    scenario("navigating to error page") {
      When("user hits the error page")
      val response = client("/error").get().futureValue

      Then("the title should be the expected one")
      val html = Jsoup.parse(response.body)
      html.select("h1").first().text() shouldBe "This link has expired"

      And("there should be a message after the title")
      html.select("p").iterator().asScala.toList.map(_.text()) should contain("For security reasons the link must be used within a time limit.")
    }
  }

  private def client(path: String) = WS.url(s"http://localhost:$port/email-verification$path").withFollowRedirects(false)

}
