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

package testOnly.models

import testOnly.forms.StartForm
import play.api.libs.json.{Format, Json, Writes}

case class VerifyEmailRequest(
  credId:                       String,
  continueUrl:                  String,
  origin:                       String,
  deskproServiceName:           Option[String],
  accessibilityStatementUrl:    String,
  email:                        Option[Email],
  labels:                       Option[EmailLabels],
  lang:                         String,
  useNewGovUkServiceNavigation: Boolean
)

case class Email(address: String, enterUrl: String)

object Email {
  implicit val format: Format[Email] = Json.format[Email]
}

case class EmailLabels(en: EmailLabel, cy: EmailLabel)
case class EmailLabel(pageTitle: Option[String], userFacingServiceName: Option[String])

object VerifyEmailRequest {
  implicit val labelWrites:  Writes[EmailLabel] = Json.writes
  implicit val labelsWrites: Writes[EmailLabels] = Json.writes
  implicit val writes:       Writes[VerifyEmailRequest] = Json.writes

  def fromStartForm(credId: String, startForm: StartForm): VerifyEmailRequest = {
    VerifyEmailRequest(
      credId                    = credId,
      continueUrl               = startForm.continueUrl,
      origin                    = startForm.origin,
      deskproServiceName        = startForm.deskproServiceName,
      accessibilityStatementUrl = startForm.accessibilityStatementUrl,
      email                     = startForm.email.map(Email(_, startForm.emailEntryUrl.getOrElse(""))),
      labels                    = Some(EmailLabels(en = EmailLabel(startForm.pageTitleEnLabel, Some("Team Signature")), cy = EmailLabel(None, None))),
      lang                      = startForm.lang,
      useNewGovUkServiceNavigation = startForm.useNewGovUkServiceNavigation
    )
  }
}
