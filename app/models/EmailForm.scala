/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.i18n.Messages

case class EmailForm(email: String, continue: String)
object EmailForm {
  def form(implicit messages: Messages): Form[EmailForm] = Form(mapping(
    "email" -> text.verifying(messages("emailform.error.invalidEmailFormat"), _.matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")),
    "continue" -> text
  )(EmailForm.apply)(EmailForm.unapply))
}
