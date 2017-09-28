/*
 * Copyright 2017 HM Revenue & Customs
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

import uk.gov.hmrc.http.{HttpGet, HttpPost}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector => Auditing}
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.http.ws._

object FrontendAuditConnector extends Auditing with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

trait WSHttp extends HttpGet with HttpPost with HttpAuditing

object WSHttp extends WSHttp with WSGet with WSPost with AppName {
  val auditConnector = FrontendAuditConnector
  override val hooks = Seq(AuditingHook)
}
