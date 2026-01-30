/*
 * Copyright 2024 HM Revenue & Customs
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

package testOnly.connectors

import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import testOnly.models.{VerificationStatusResponse, VerifyEmailRequest}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyEmailVerificationConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {

  private val baseUrl = servicesConfig.baseUrl("email-verification")

  @nowarn
  def requestEmailVerification(verifyEmailRequest: VerifyEmailRequest)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .post(url"$baseUrl/email-verification/verify-email")
      .withBody(Json.toJson(verifyEmailRequest))
      .execute[HttpResponse]
      .map {
        case HttpResponse(201, body, _) =>
          val json = Json.parse(body)
          (json \ "redirectUri").as[String]
        case HttpResponse(other, body, _) =>
          throw UpstreamErrorResponse(s"bad stuff happened when calling POST /email-verification/verify-email:\n$body", other)
      }

  def verificationStatus(credId: String)(implicit hc: HeaderCarrier): Future[VerificationStatusResponse] =
    httpClient
      .get(url"$baseUrl/email-verification/verification-status/$credId")
      .execute[VerificationStatusResponse]

}
