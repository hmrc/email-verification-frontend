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

import javax.inject.{Inject, Singleton}
import models.{EmailPasscodeException, Journey, PasscodeRequest, PasscodeVerificationRequest}
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class VerificationToken(token: String)

object VerificationToken {
  implicit val writes: Writes[VerificationToken] = Json.writes[VerificationToken]
}

@Singleton
class EmailVerificationConnector @Inject() (
  http:              HttpClient,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Logging {
  private lazy val serviceUrl = frontendAppConfig.emailUrl

  def verifyEmailAddress(token: String)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
    http
      .POST[VerificationToken, Either[UpstreamErrorResponse, Unit]](s"$serviceUrl/email-verification/verified-email-addresses",
                                                                    VerificationToken(token),
                                                                    Nil
                                                                   )
      .map {
        case Left(err) => throw err
        case Right(_)  => ()
      }

  def requestPasscode(email: String, lang: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http
      .POST[PasscodeRequest, HttpResponse](
        s"$serviceUrl/email-verification/request-passcode",
        PasscodeRequest(email, "email-verification-frontend", lang)
      )
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
      .POST[PasscodeVerificationRequest, HttpResponse](
        s"$serviceUrl/email-verification/verify-passcode",
        PasscodeVerificationRequest(email, passcode)
      )
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
      .POST[JsValue, HttpResponse](
        s"$serviceUrl/email-verification/journey/$journeyId/email",
        Json.obj("email" -> email)
      )
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
    http.POSTEmpty[HttpResponse](s"$serviceUrl/email-verification/journey/$journeyId/resend-passcode").map { response =>
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
      .POST[JsValue, HttpResponse](
        s"$serviceUrl/email-verification/journey/$journeyId/passcode",
        Json.obj("passcode" -> passcode)
      )
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
    http.GET[Option[Journey]](s"$serviceUrl/email-verification/journey/$journeyId")
  }
}

sealed trait SubmitEmailResponse
object SubmitEmailResponse {
  case object Accepted                            extends SubmitEmailResponse
  case object JourneyNotFound                     extends SubmitEmailResponse
  case class TooManyAttempts(continueUrl: String) extends SubmitEmailResponse

  private val tooManyAttemptsReads: Reads[TooManyAttempts] = Json.reads[TooManyAttempts]

  implicit val reads: Reads[SubmitEmailResponse] = (json: JsValue) =>
    (json \ "status").validate[String].flatMap {
      case "accepted"        => JsSuccess(SubmitEmailResponse.Accepted)
      case "journeyNotFound" => JsSuccess(SubmitEmailResponse.JourneyNotFound)
      case "tooManyAttempts" => tooManyAttemptsReads.reads(json)
      case other             => JsError(s"unexpected SubmitEmailResponse `status` $other")
    }
}

sealed trait ValidatePasscodeResponse
object ValidatePasscodeResponse {
  case class Complete(continueUrl: String)        extends ValidatePasscodeResponse
  case class IncorrectPasscode(journey: Journey)  extends ValidatePasscodeResponse
  case class TooManyAttempts(continueUrl: String) extends ValidatePasscodeResponse
  case object JourneyNotFound                     extends ValidatePasscodeResponse

  private val completeReads:          Reads[Complete] = Json.reads[Complete]
  private val incorrectPasscodeReads: Reads[IncorrectPasscode] = Json.reads[IncorrectPasscode]
  private val tooManyAttemptsReads:   Reads[TooManyAttempts] = Json.reads[TooManyAttempts]

  implicit val reads: Reads[ValidatePasscodeResponse] = (json: JsValue) =>
    (json \ "status").validate[String].flatMap {
      case "complete"          => completeReads.reads(json)
      case "incorrectPasscode" => incorrectPasscodeReads.reads(json)
      case "tooManyAttempts"   => tooManyAttemptsReads.reads(json)
      case "journeyNotFound"   => JsSuccess(ValidatePasscodeResponse.JourneyNotFound)
      case other               => JsError(s"unexpected ValidatePasscodeResponse `status` $other")
    }
}

sealed trait ResendPasscodeResponse
object ResendPasscodeResponse {
  case object PasscodeResent                               extends ResendPasscodeResponse
  case object JourneyNotFound                              extends ResendPasscodeResponse
  case object NoEmailProvided                              extends ResendPasscodeResponse
  case class TooManyAttemptsInSession(continueUrl: String) extends ResendPasscodeResponse
  case class TooManyAttemptsForEmail(journey: Journey)     extends ResendPasscodeResponse

  private val tooManyAttemptsInSessionReads: Reads[TooManyAttemptsInSession] = Json.reads[TooManyAttemptsInSession]
  private val tooManyAttemptsForEmail:       Reads[TooManyAttemptsForEmail] = Json.reads[TooManyAttemptsForEmail]

  implicit val reads: Reads[ResendPasscodeResponse] = (json: JsValue) =>
    (json \ "status").validate[String].flatMap {
      case "passcodeResent"           => JsSuccess(PasscodeResent)
      case "tooManyAttemptsInSession" => tooManyAttemptsInSessionReads.reads(json)
      case "tooManyAttemptsForEmail"  => tooManyAttemptsForEmail.reads(json)
      case "journeyNotFound"          => JsSuccess(ResendPasscodeResponse.JourneyNotFound)
      case "noEmailProvided"          => JsSuccess(NoEmailProvided)
      case other                      => JsError(s"unexpected ResendPasscodeResponse `status` $other")
    }
}
