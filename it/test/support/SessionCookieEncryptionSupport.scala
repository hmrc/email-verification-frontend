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

package support

import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.WSRequest
import play.api.test.{HasApp, Injecting}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, PlainText, SymmetricCryptoFactory}

trait SessionCookieEncryptionSupport extends Injecting {
  self: HasApp =>

  val signer: CookieSigner = inject[CookieSigner]
  val SignSeparator = "-"
  val mdtpSessionCookie = "mdtp"

  lazy val cipher: Encrypter with Decrypter = SymmetricCryptoFactory.aesGcmCryptoFromConfig("cookie.encryption", inject[Configuration].underlying)

  private def createPopulatedSessionCookie(payload: String): String = {
    val signedPayload = signer.sign(payload) + SignSeparator + payload
    val encryptedSignedPayload: String = cipher.encrypt(PlainText(signedPayload)).value
    s"""$mdtpSessionCookie=$encryptedSignedPayload"""
  }

  implicit class WSRequestWithSession(request: WSRequest) {
    def withSession(pair: (String, String)*): WSRequest =
      request.addHttpHeaders(
        HeaderNames.COOKIE -> createPopulatedSessionCookie(
          pair.toSeq.map { case (k, v) => s"$k=$v" }.mkString("&")
        )
      )
  }

}
