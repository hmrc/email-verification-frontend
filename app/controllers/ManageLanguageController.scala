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

package controllers

import config.{ErrorHandler, FrontendAppConfig}
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.text
import play.api.http.HeaderNames
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc._
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}
import views.Views

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success}

@Singleton
class ManageLanguageController @Inject() (
  config: FrontendAppConfig,
  languageUtils: LanguageUtils,
  views: Views,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  messagesApi: MessagesApi
) extends LanguageController(languageUtils, mcc) with Logging {

  protected def fallbackURL = "/"

  protected def languageMap: Map[String, Lang] = config.getAvailableLanguages

  def switchToLang: String => Action[AnyContent] = (lang: String) => switchToLanguage(lang)

  private def langFromName(request: Request[_], languageName: String): Lang = {
    val languageMap = config.getAvailableLanguages
    val enabled: Boolean = languageMap.get(languageName).exists(languageUtils.isLangAvailable)
    if (enabled) {
      languageMap.getOrElse(languageName, languageUtils.getCurrentLang(request))
    } else {
      languageUtils.getCurrentLang(request)
    }
  }

  private def requestWithLanguage(request: Request[_], lang: Lang): Request[_] = {
    val updatedCookies = request.cookies.toSeq.filter(_.name != messagesApi.langCookieName) :+ Cookie(messagesApi.langCookieName, lang.code)
    val updatedCookiesHeader = Cookies.encodeCookieHeader(updatedCookies)
    val updatedHeaders = request.headers.replace((HeaderNames.COOKIE, updatedCookiesHeader))
    request.addAttr(RequestAttrKey.Cookies, Cell(Cookies(updatedCookies))).withTransientLang(lang.code).withHeaders(updatedHeaders)
  }

  def showViewWithLanguage(languageName: String, view: String): Action[AnyContent] = Action { implicit request =>

    val lang = langFromName(request, languageName)

    val formDataFromRequest = (request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined =>
        body.asMultipartFormData.get.asFormUrlEncoded
    }).map(tuple => tuple._1 -> tuple._2.headOption.getOrElse(""))

    //just using this to push received data into the view, mapping isn't used
    val formData = Form[String](mapping = text, data = formDataFromRequest, errors = Nil, value = None)
    val requestWithUpdatedLang = requestWithLanguage(request, lang)
    val messagesForUpdatedLang = messagesApi.preferred(requestWithUpdatedLang)

    views.render(view, formData)(requestWithUpdatedLang, messagesForUpdatedLang, config) match {
      case Success(content) => {
        Ok(content).withCookies(Cookie(messagesApi.langCookieName, lang.code))
      }
      case Failure(exception) => {
        logger.error(s"Failed to render view '$view'", exception)
        BadRequest(errorHandler.badRequestTemplate)
      }
    }
  }
}
