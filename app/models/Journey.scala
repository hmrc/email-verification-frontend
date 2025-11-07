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

package models

import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.RequestHeader

case class Journey(
  accessibilityStatementUrl:    String,
  deskproServiceName:           String,
  enterEmailUrl:                Option[String],
  backUrl:                      Option[String],
  serviceTitle:                 Option[String],
  emailAddress:                 Option[String],
  labels:                       Option[MessageLabels],
  useNewGovUkServiceNavigation: Boolean
) {

  def serviceTitleMessage(implicit request: RequestHeader): Option[String] = {

    val isWelsh = request.cookies.get("PLAY_LANG").map(_.value).contains("cy")
    val welshTitle = labels.flatMap(_.cy.pageTitle)
    val englishTitle = labels.flatMap(_.en.pageTitle)

    if (isWelsh && welshTitle.isDefined) {
      welshTitle
    } else if (englishTitle.isDefined) {
      englishTitle
    } else {
      serviceTitle
    }
  }
}

object Journey {
  implicit val reads:  Reads[Journey] = Json.reads[Journey]
  implicit val writes: Writes[Journey] = Json.writes[Journey]
}
