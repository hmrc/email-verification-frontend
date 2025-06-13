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
