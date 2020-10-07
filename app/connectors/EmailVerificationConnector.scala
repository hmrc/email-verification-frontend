/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.{EmailPasscodeException, PasscodeRequest, PasscodeVerificationRequest}
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

case class VerificationToken(token: String)

object VerificationToken {
  implicit val writes: Writes[VerificationToken] = Json.writes[VerificationToken]
}

@Singleton
class EmailVerificationConnector @Inject() (
  http: HttpClient,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext) extends Logging {
  private lazy val serviceUrl = frontendAppConfig.emailUrl

  def verifyEmailAddress(token: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
    http.POST[VerificationToken, Either[UpstreamErrorResponse, Unit]](s"$serviceUrl/email-verification/verified-email-addresses", VerificationToken(token), Nil)
      .map {
        case Left(err) => throw err
        case Right(_)  => ()
      }

  def requestPasscode(email: String, lang: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[PasscodeRequest, HttpResponse](
      s"$serviceUrl/email-verification/request-passcode",
      PasscodeRequest(email, "email-verification-frontend", lang)
    ).map {
        case r @ HttpResponse(201, _, _) => ()
        case r @ HttpResponse(401, _, _) => throw new EmailPasscodeException.MissingSessionId(r.body)
        case r @ HttpResponse(403, _, _) => throw new EmailPasscodeException.MaxNewEmailsExceeded(r.body)
        case r @ HttpResponse(409, _, _) => throw new EmailPasscodeException.EmailAlreadyVerified(r.body)
        case r: HttpResponse             => throw new EmailPasscodeException.EmailVerificationServerError(r.body)
      }
  }

  def verifyPasscode(email: String, passcode: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[PasscodeVerificationRequest, HttpResponse](
      s"$serviceUrl/email-verification/verify-passcode", PasscodeVerificationRequest(email, passcode)
    ).map {
        case r @ HttpResponse(201, _, _) => ()
        case r @ HttpResponse(204, _, _) => ()
        case r @ HttpResponse(401, _, _) => throw new EmailPasscodeException.MissingSessionId(r.body)
        case r @ HttpResponse(403, _, _) => throw new EmailPasscodeException.MaxPasscodeAttemptsExceeded(r.body)
        case r @ HttpResponse(404, _, _) => throw new EmailPasscodeException.IncorrectPasscode(r.body)
        case r: HttpResponse             => throw new EmailPasscodeException.EmailVerificationServerError(r.body)
      }
  }

}
