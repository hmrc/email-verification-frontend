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

import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, text}

case class PasscodeForm(email: String, passcode: String, continue: String)

object PasscodeForm {
  private val passcodeField = text.verifying("passcodeform.error.invalidFormat", _.matches("^[BCDFGHJKLMNPQRSTVWXYZ]{6}$"))

  val form: Form[PasscodeForm] = Form(mapping(
    "email" -> text,
    "passcode" -> passcodeField,
    "continue" -> text
  )(PasscodeForm.apply)(PasscodeForm.unapply))

  val singleForm: Form[String] = Form(Forms.single("passcode" -> passcodeField))
}
