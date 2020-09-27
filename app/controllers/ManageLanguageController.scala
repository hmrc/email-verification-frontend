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

package controllers

import config.{ErrorHandler, FrontendAppConfig}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logging}
import play.api.data.Forms.text
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.i18n.Lang
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc.{Action, AnyContent, Cookie, Cookies, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}
import views.Views

import scala.util.{Failure, Success}

@Singleton
class ManageLanguageController @Inject() (
  config: FrontendAppConfig,
  configuration: Configuration,
  languageUtils: LanguageUtils,
  views: Views,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler
)
  extends LanguageController(configuration, languageUtils, mcc) with Logging {

  protected def fallbackURL = "/"

  protected def languageMap: Map[String, Lang] = config.getAvailableLanguages

  def switchToLang: String => Action[AnyContent] = (lang: String) => switchToLanguage(lang)

  private def langFromString(request: Request[_], language: String): Lang = {
    val languageMap = config.getAvailableLanguages
    val enabled: Boolean = languageMap.get(language).exists(languageUtils.isLangAvailable)
    if (enabled) {
      languageMap.getOrElse(language, languageUtils.getCurrentLang(request))
    } else {
      languageUtils.getCurrentLang(request)
    }
  }

  private def requestWithLanguage(request: Request[_], language: String): Request[_] = {
    val lang: Lang = langFromString(request, language)
    val updatedCookies = request.cookies.toSeq.filter(_.name != "PLAY_LANG") :+ Cookie("PLAY_LANG", lang.code)
    val updatedCookiesHeader = Cookies.encodeCookieHeader(updatedCookies)
    val updatedHeaders = request.headers.replace((HeaderNames.COOKIE, updatedCookiesHeader))
    request.addAttr(RequestAttrKey.Cookies, Cell(Cookies(updatedCookies))).withTransientLang(lang.code).withHeaders(updatedHeaders)
  }

  def showViewWithFormAndLanguage(view: String, language: String): Action[AnyContent] = Action { implicit request =>
    val formDataFromRequest = (request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined =>
        body.asMultipartFormData.get.asFormUrlEncoded
    }).map(tuple => tuple._1 -> tuple._2.headOption.getOrElse(""))

    //just using this to push received data into the view, mapping isn't used
    val form = Form[String](mapping = text, data = formDataFromRequest, errors = Nil, value = None)

    views.render(view, form)(requestWithLanguage(request, language), implicitly, config) match {
      case Success(content) => Ok(content)
      case Failure(exception) => {
        logger.error(s"Failed to render view '$view'", exception)
        BadRequest(errorHandler.badRequestTemplate)
      }
    }
  }
}
