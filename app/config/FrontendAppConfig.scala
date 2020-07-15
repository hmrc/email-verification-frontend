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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig, runMode: RunMode) {

  private lazy val contactFormServiceIdentifier = "email-verification-frontend"

  lazy val isWelshEnabled: Boolean = configuration.getOptional[Boolean]("features.welsh-translation").getOrElse(true)

  def getAvailableLanguages: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  lazy val analyticsToken: String = servicesConfig.getString(s"${runMode.env}.google-analytics.token")
  lazy val analyticsHost: String = servicesConfig.getString(s"${runMode.env}.google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val emailUrl: String = servicesConfig.baseUrl("email-verification")
}