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

package testOnly.forms

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}

case class StartForm(email:                        Option[String],
                     continueUrl:                  String,
                     origin:                       String,
                     accessibilityStatementUrl:    String,
                     deskproServiceName:           Option[String],
                     emailEntryUrl:                Option[String],
                     pageTitleEnLabel:             Option[String],
                     lang:                         String,
                     useNewGovUkServiceNavigation: Boolean
                    ) {}
object StartForm {

  def apply(email:                        String,
            continueUrl:                  String,
            origin:                       String,
            accessibilityStatementUrl:    String,
            deskproServiceName:           String,
            emailEntryUrl:                String,
            pageTitleEnLabel:             String,
            lang:                         String,
            useNewGovUkServiceNavigation: Boolean
           ): StartForm = {
    new StartForm(
      if (email.trim.isEmpty) None else Some(email.trim),
      continueUrl,
      origin,
      accessibilityStatementUrl,
      if (deskproServiceName.trim.isEmpty) None else Some(deskproServiceName.trim),
      if (emailEntryUrl.trim.isEmpty) None else Some(emailEntryUrl.trim),
      if (pageTitleEnLabel.trim.isEmpty) None else Some(pageTitleEnLabel.trim),
      lang,
      useNewGovUkServiceNavigation
    )
  }

  def unapply(startForm: StartForm): Option[(String, String, String, String, String, String, String, String, Boolean)] = {
    Some(
      (startForm.email.getOrElse(""),
       startForm.continueUrl,
       startForm.origin,
       startForm.accessibilityStatementUrl,
       startForm.deskproServiceName.getOrElse(""),
       startForm.emailEntryUrl.getOrElse(""),
       startForm.pageTitleEnLabel.getOrElse(""),
       startForm.lang,
       startForm.useNewGovUkServiceNavigation
      )
    )
  }

  val form: Form[StartForm] = {
    Form(
      mapping(
        "email" -> text.verifying("startform.error.invalidEmailFormat",
                                  s => s.trim.isEmpty || s.matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")
                                 ),
        "continueUrl"               -> text.verifying("startform.error.missingContinueUrl", _.trim.nonEmpty),
        "origin"                    -> text.verifying("startform.error.missingOrigin", _.trim.nonEmpty),
        "accessibilityStatementUrl" -> text.verifying("startform.error.missingAccessibilityStatementUrl", _.trim.nonEmpty),
        "deskproServiceName"        -> text,
        "emailEntryUrl"             -> text,
        "pageTitleEnLabel"          -> text,
        "lang"                      -> text.verifying("startform.error.missingLang", _.trim.nonEmpty),
        "useNewGovUkServiceNavigation" -> optional(text)
          .verifying("startform.error.missingServiceNavigation", _.isDefined)
          .transform[Boolean](_.contains("true"), b => if (b) Some("true") else Some("false"))
      )(StartForm.apply)(StartForm.unapply)
    )
  }
}
