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

package views

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.play.language.LanguageUtils

import scala.util.{Failure, Try}

class Views @Inject() (
  appConfig: FrontendAppConfig,
  languageUtils: LanguageUtils,
  val errorTemplate: views.html.MessagePage,
  val verifyError: views.html.verify_error,
  val emailForm: views.html.EmailForm,
  val passcodeForm: views.html.PasscodeForm,

  val success: views.html.Success,
  val emailAlreadyVerified: views.html.EmailAlreadyVerified,
  val emailLimitReached: views.html.EmailLimitReached,
  val passcodeLimitReached: views.html.PasscodeLimitReached
) {

  def render(view: String, form: Form[_])(implicit request: Request[_], messages: Messages, appConfig: FrontendAppConfig) = {
    view match {
      case "passcodeForm" => Try(passcodeForm(form))
      case _              => Failure(new Exception(s"Invalid view name: $view"))
    }
  }

}
