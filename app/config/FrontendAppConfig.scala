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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  lazy val allowRelativeUrls: Boolean = configuration.getOptional[String]("platform.frontend.host").isEmpty //platform.frontend.host only specified in environments not application config
  private lazy val contactFormServiceIdentifier = "email-verification-frontend"

  lazy val isWelshEnabled: Boolean = configuration.getOptional[Boolean]("features.welsh-translation").getOrElse(true)

  val footerLinkItems: Seq[String] = configuration.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  def getAvailableLanguages: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  lazy val googleTagManagerIdAvailable: Boolean = configuration.getOptional[Boolean]("google-tag-manager.id-available").getOrElse(false)
  lazy val googleTagManagerId: String = configuration.get[String]("google-tag-manager.id")

  lazy val analyticsToken: String = servicesConfig.getString("google-analytics.token")
  lazy val analyticsHost: String = servicesConfig.getString("google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val emailUrl: String = servicesConfig.baseUrl("email-verification")

}
