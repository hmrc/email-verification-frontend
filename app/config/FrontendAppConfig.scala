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

import models.Journey
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  lazy val isAppRunningOnLocalDevMachine: Boolean =
    configuration
      .getOptional[String]("platform.frontend.host")
      .isEmpty // platform.frontend.host only specified in environments not application config

  lazy val googleTagManagerIdAvailable: Boolean = configuration.getOptional[Boolean]("google-tag-manager.id-available").getOrElse(false)
  lazy val googleTagManagerId:          String = configuration.get[String]("google-tag-manager.id")

  lazy val emailUrl: String = servicesConfig.baseUrl("email-verification")

  private lazy val serviceNavigationFeatureEnabled: Boolean =
    configuration.getOptional[Boolean]("features.forceServiceNavigation").getOrElse(false)

  def forceServiceNavigation(journey: Journey): Boolean =
    serviceNavigationFeatureEnabled && journey.useNewGovUkServiceNavigation

  lazy val mdtpInternalDomains: Set[String] = servicesConfig.getString("mdtp.internalDomains").split(",").toSet

  lazy val timeoutConfig: TimeoutConfig = {
    TimeoutConfig(
      timeoutSeconds = configuration.get[Int]("timeoutDialog.timeout"),
      countdownSecs  = configuration.get[Int]("timeoutDialog.countdown"),
      signOutUrl     = "/bas-gateway/sign-out-without-state",
      signInUrl      = "/bas-gateway/sign-in"
    )
  }
}

case class TimeoutConfig(timeoutSeconds: Int, countdownSecs: Int, signOutUrl: String, signInUrl: String)
