/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.emailverification

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  def analyticsToken: String
  def analyticsHost: String
  def reportAProblemPartialUrl: String
  def reportAProblemNonJSUrl: String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactFrontendUrl = baseUrl("contact-frontend")
  private val contactFormServiceIdentifier = "email-verification-frontend"

  override lazy val analyticsToken = loadConfig(s"$env.google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"$env.google-analytics.host")
  override lazy val reportAProblemPartialUrl = s"$contactFrontendUrl/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
}
