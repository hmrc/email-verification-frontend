/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.emailverification.connectors

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.emailverification.WSHttp
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

case class VerificationToken(token: String)

object VerificationToken {
  implicit val writes: Writes[VerificationToken] = Json.writes[VerificationToken]
}

trait EmailVerificationConnector {
  def http: HttpPost
  def serviceUrl: String
  def verifyEmailAddress(token: String)
                        (implicit headerCarrier: HeaderCarrier) =
    http.POST(s"$serviceUrl/email-verification/verified-email-addresses", VerificationToken(token), Nil).map(_ => {})
}

object EmailVerificationConnector extends EmailVerificationConnector with ServicesConfig {
  override lazy val http = WSHttp
  override val serviceUrl = baseUrl("email-verification")
}
