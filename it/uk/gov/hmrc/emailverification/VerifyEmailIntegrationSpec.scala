package uk.gov.hmrc.emailverification

import java.util.UUID

import org.joda.time.DateTime.now
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import org.scalatestplus.play.OneServerPerSuite
import play.api.http.HeaderNames
import play.api.libs.Crypto
import play.api.libs.ws.WS

class VerifyEmailIntegrationSpec extends FeatureSpec with GivenWhenThen with OneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers {

  feature("verify email") {

    scenario("link is not expired") {

      Given("an encrypted payload containing a nonce token, email, expiry time and a continue url")
      val email = "john@doe.com"
      val continueUrl = "/continue-url"
      val expiryTime = now().plusDays(1)

      val jsonToken =
        s"""
           |{
           | "token": s"${UUID.randomUUID()}",
           | "email": "$email",
           | "continueUrl": "$continueUrl",
           | "expiryTime" : s"$expiryTime"
           |}
        """.stripMargin
      val encryptedJsonToken = Crypto.encryptAES(jsonToken)

      When("call GET on verify url passing the encrypted payload as token")
      val response = WS.url(s"http://localhost:$port/email-verification/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the continue url")
      response.header(HeaderNames.LOCATION) should contain("/continue-url")
    }

    scenario("link is expired") {
      Given("an encrypted payload containing a nonce token, email, expiry time and a continue url")
      val email = "john@doe.com"
      val continueUrl = "/continue-url"
      val expiryTime = now().minusDays(1)

      val jsonToken =
        s"""
           |{
           | "token": s"${UUID.randomUUID()}",
           | "email": "$email",
           | "continueUrl": "$continueUrl",
           | "expiryTime" : s"$expiryTime"
           |}
        """.stripMargin
      val encryptedJsonToken = Crypto.encryptAES(jsonToken)

      When("call GET on verify url passing the encrypted payload as token")
      val response = WS.url(s"http://localhost:$port/email-verification/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the continue url")
      response.header(HeaderNames.LOCATION) should contain("/error")
    }
  }

}
