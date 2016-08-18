package uk.gov.hmrc.emailverification

import java.util.UUID

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import org.scalatestplus.play.OneServerPerSuite
import play.api.http.HeaderNames
import play.api.libs.ws.WS
import uk.gov.hmrc.crypto.ApplicationCrypto._
import uk.gov.hmrc.crypto.PlainText

class VerifyEmailIntegrationSpec extends FeatureSpec with GivenWhenThen with OneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers {

  feature("verify email") {
    val email = "john@doe.com"
    val continueUrl = "/continue-url"

    def jsonToken(expiryTime : DateTime) =
      s"""
         |{
         | "nonce": "${UUID.randomUUID()}",
         | "email": "$email",
         | "continueUrl": "$continueUrl",
         | "expiration" : "$expiryTime"
         |}
        """.stripMargin
    def encrypt(value : String) = QueryParameterCrypto.encrypt(PlainText(value)).value

    scenario("link is not expired") {

      Given("an encrypted payload containing a nonce token, email, expiry time in future and a continue url")
      val expiryTime = now().plusDays(1)
      val encryptedJsonToken = encrypt(jsonToken(expiryTime))

      When("call GET on verify url passing the encrypted payload as token")
      val response = client("/verify").withQueryString("token" -> encryptedJsonToken).get().futureValue

      Then("response status should be 303 redirect")
      response.status shouldBe 303

      And("response Location header should be the continue url")
      response.header(HeaderNames.LOCATION) should contain("/continue-url")
    }

    scenario("link is expired") {
      Given("an encrypted payload containing a nonce token, email, expiry time in past and a continue url")
      val expiryTime = now().minusDays(1)
      val encryptedJsonToken = encrypt(jsonToken(expiryTime))

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
