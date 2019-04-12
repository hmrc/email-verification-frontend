/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.play.config.ServicesConfig

//trait AppConfig {
//
//  protected def configuration: Configuration
//  protected def env: String
//
//  lazy val analyticsToken: String = loadConfig(s"$env.google-analytics.token")
//  lazy val analyticsHost: String = loadConfig(s"$env.google-analytics.host")
//  lazy val reportAProblemPartialUrl: String = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
//  lazy val reportAProblemNonJSUrl: String = s"/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
//
//  private lazy val contactFormServiceIdentifier = "email-verification-frontend"
//
//  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
//}
//
//object FrontendAppConfig extends AppConfig with ServicesConfig {
//  override protected lazy val configuration = Play.current.configuration
//}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, environment: Environment) extends ServicesConfig {
  override protected def mode = environment.mode

  override protected def runModeConfiguration = configuration

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val contactFormServiceIdentifier = "email-verification-frontend"

  lazy val analyticsToken: String = loadConfig(s"$env.google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"$env.google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
}