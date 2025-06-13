/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import play.api.libs.json._

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
