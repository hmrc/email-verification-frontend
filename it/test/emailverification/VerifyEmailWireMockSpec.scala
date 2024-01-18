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

package test.emailverification

import java.util.{Base64, UUID}
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Injecting
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.gg.test.WireMockSpec
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.TableDrivenPropertyChecks._

class VerifyEmailWireMockSpec extends WireMockSpec with Injecting {
  private val continueUrl = "/continue-url"
  private def jsonToken(token: String) = Json
    .obj(
      "token"       -> token,
      "continueUrl" -> continueUrl
    )
    .toString

  def encryptAndEncode(value: String) = new String(inject[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(value)).toBase64)

  def encryptAndEncodeWithUrlEncoder(value: String) = new String(
    Base64.getUrlEncoder.withoutPadding().encode(inject[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(value)).value.getBytes("UTF-8"))
  )

  "a verification link with a valid encrypted payload" should {

    val encodedStrings = Table[String, String => String, String](
      ("counter", "encoder", "token"),
      ("1", encryptAndEncode, Gen.listOfN(16, Arbitrary.arbChar.arbitrary).map(_.mkString).sample.get),
      ("2", encryptAndEncodeWithUrlEncoder, Gen.listOfN(16, Arbitrary.arbChar.arbitrary).map(_.mkString).sample.get)
    )

    forAll(encodedStrings) { (counter, encoder, token) =>
      s"redirect to the original continue URL $counter" in {
        val encryptedJsonToken = encoder(jsonToken(token))

        stubFor(
          post(urlEqualTo("/email-verification/verified-email-addresses"))
            .willReturn(created())
        )

        val response = await(
          resourceRequest("/email-verification/verify")
            .addQueryStringParameters("token" -> encryptedJsonToken)
            .withFollowRedirects(false)
            .get()
        )

        response.status                     shouldBe SEE_OTHER
        response.header(HeaderNames.LOCATION) should contain("/continue-url")

        verify(postRequestedFor(urlEqualTo("/email-verification/verified-email-addresses")))
      }
    }
  }

  "a verification link with an invalid token" should {
    "redirect to the error page" in {
      val token = UUID.randomUUID().toString
      val encryptedJsonToken = encryptAndEncode(jsonToken(token))

      stubFor(
        post(urlEqualTo("/email-verification/verified-email-addresses"))
          .willReturn(badRequest())
      )

      val response = await(
        resourceRequest("/email-verification/verify")
          .addQueryStringParameters("token" -> encryptedJsonToken)
          .withFollowRedirects(false)
          .get()
      )

      response.status                     shouldBe SEE_OTHER
      response.header(HeaderNames.LOCATION) should contain("/email-verification/error")
    }
  }
}
