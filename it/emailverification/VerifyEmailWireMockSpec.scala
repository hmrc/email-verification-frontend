package emailverification

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Injecting
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.gg.test.WireMockSpec

class VerifyEmailWireMockSpec extends WireMockSpec with Injecting {

  val continueUrl = "/continue-url"

  def jsonToken(token: String) = Json.obj(
    "token" -> token,
    "continueUrl" -> continueUrl
  ).toString

  def encryptAndEncode(value: String) = new String(inject[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(value)).toBase64)

  "a verification link with a valid encrypted payload" should {
    "redirect to the original continue URL" in {
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encryptAndEncode(jsonToken(token))

      stubFor(
        post(
          urlEqualTo("/email-verification/verified-email-addresses"))
          .willReturn(created())
      )

      val response = await(
        resourceRequest("/email-verification/verify")
          .addQueryStringParameters("token" -> encryptedJsonToken)
          .withFollowRedirects(false)
          .get()
      )

      response.status shouldBe SEE_OTHER
      response.header(HeaderNames.LOCATION) should contain("/continue-url")

      verify(postRequestedFor(urlEqualTo("/email-verification/verified-email-addresses")))
    }
  }

  "a verification link with an invalid token" should {
    "redirect to the error page" in {
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encryptAndEncode(jsonToken(token))

      stubFor(
        post(
          urlEqualTo("/email-verification/verified-email-addresses"))
          .willReturn(badRequest())
      )

      val response = await(
        resourceRequest("/email-verification/verify")
          .addQueryStringParameters("token" -> encryptedJsonToken)
          .withFollowRedirects(false)
          .get()
      )

      response.status shouldBe SEE_OTHER
      response.header(HeaderNames.LOCATION) should contain("/email-verification/error")
    }
  }
}
