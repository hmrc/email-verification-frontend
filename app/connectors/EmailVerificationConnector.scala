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

package connectors

import config.FrontendAppConfig
import models._
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class EmailVerificationConnector @Inject() (http: HttpClientV2, frontendAppConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private lazy val serviceUrl = frontendAppConfig.emailUrl

  def verifyEmailAddress(token: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    http
      .post(url"$serviceUrl/email-verification/verified-email-addresses")
      .withBody(Json.toJson(VerificationToken(token)))
      .execute[Either[UpstreamErrorResponse, Unit]]
      .map {
        case Left(err) => throw err
        case Right(_)  => ()
      }
  }

  def requestPasscode(email: String, lang: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http
      .post(url"$serviceUrl/email-verification/request-passcode")
      .withBody(Json.toJson(PasscodeRequest(email, "email-verification-frontend", lang)))
      .execute[HttpResponse]
      .map {
        case r @ HttpResponse(201, _, _) => ()
        case r @ HttpResponse(401, _, _) => throw EmailPasscodeException.MissingSessionId(r.body)
        case r @ HttpResponse(403, _, _) => throw EmailPasscodeException.MaxNewEmailsExceeded(r.body)
        case r @ HttpResponse(409, _, _) => throw EmailPasscodeException.EmailAlreadyVerified(r.body)
        case r: HttpResponse => throw EmailPasscodeException.EmailVerificationServerError(r.body)
      }
  }

  def verifyPasscode(email: String, passcode: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http
      .post(url"$serviceUrl/email-verification/verify-passcode")
      .withBody(Json.toJson(PasscodeVerificationRequest(email, passcode)))
      .execute[HttpResponse]
      .map {
        case HttpResponse(201, _, _)     => ()
        case HttpResponse(204, _, _)     => ()
        case r @ HttpResponse(401, _, _) => throw EmailPasscodeException.MissingSessionId(r.body)
        case r @ HttpResponse(403, _, _) => throw EmailPasscodeException.MaxPasscodeAttemptsExceeded(r.body)
        case r @ HttpResponse(404, _, _) => throw EmailPasscodeException.IncorrectPasscode(r.body)
        case r: HttpResponse => throw EmailPasscodeException.EmailVerificationServerError(r.body)
      }
  }

  def submitEmail(journeyId: String, email: String)(implicit hc: HeaderCarrier): Future[SubmitEmailResponse] = {
    http
      .post(url"$serviceUrl/email-verification/journey/$journeyId/email")
      .withBody(Json.obj("email" -> email))
      .execute[HttpResponse]
      .map { response =>
        Try(response.json) match {
          case Success(json) =>
            json.as[SubmitEmailResponse]
          case Failure(_) =>
            throw UpstreamErrorResponse(s"Expected POST /journey/$journeyId/email to return JSON; got: ${response.body}", response.status)
        }
      }
  }

  def resendPasscode(journeyId: String)(implicit hc: HeaderCarrier): Future[ResendPasscodeResponse] = {
    http
      .post(url"$serviceUrl/email-verification/journey/$journeyId/resend-passcode")
      .withBody(Json.obj())
      .execute[HttpResponse]
      .map { response =>
        Try(response.json) match {
          case Success(json) =>
            json.as[ResendPasscodeResponse]
          case Failure(_) =>
            throw UpstreamErrorResponse(s"Expected POST /journey/$journeyId/resend-passcode to return JSON; got: ${response.body}", response.status)
        }
      }
  }

  def validatePasscode(journeyId: String, passcode: String)(implicit hc: HeaderCarrier): Future[ValidatePasscodeResponse] = {
    http
      .post(url"$serviceUrl/email-verification/journey/$journeyId/passcode")
      .withBody(Json.obj("passcode" -> passcode))
      .execute[HttpResponse]
      .map { response =>
        Try(response.json) match {
          case Success(json) =>
            json.as[ValidatePasscodeResponse]
          case Failure(_) =>
            throw UpstreamErrorResponse(s"Expected POST /journey/$journeyId/passcode to return JSON; got: ${response.body}", response.status)
        }
      }
  }

  def getJourney(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Journey]] = {
    http
      .get(url"$serviceUrl/email-verification/journey/$journeyId")
      .execute[Option[Journey]]
  }
}
