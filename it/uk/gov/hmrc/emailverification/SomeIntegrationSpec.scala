package uk.gov.hmrc.emailverification

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import play.api.libs.Crypto
import play.api.libs.ws.WS
import uk.gov.hmrc.play.test.WithFakeApplication

class SomeIntegrationSpec extends FeatureSpec with GivenWhenThen with WithFakeApplication with ScalaFutures with IntegrationPatience with Matchers {

  implicit val application = fakeApplication

  feature("verify email") {

    scenario("token is valid") {

      Given("an encrypted payload containing a token and a continue url")
      val token = "token"
      val continueUrl = "/continue-url"

      val jsonToken =
        s"""
           |{
           | "token": "$token",
           | "continueUrl": "$continueUrl"
           |}
        """.stripMargin
      val encryptedJsonToken = Crypto.encryptAES(jsonToken)

      When("call GET on verify url passing the encrypted payload as token")
      val response = WS.url(s"/email-verification/verify?token=$encryptedJsonToken").get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the continue url")
      response.header("Location") should contain("/continue-url")
    }

    scenario("token is invalid") {}
  }

}
