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

package controllers

import connectors.EmailVerificationConnector
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Views

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class KeepAliveController @Inject() (
  mcc:                        MessagesControllerComponents,
  emailVerificationConnector: EmailVerificationConnector,
  views:                      Views
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) {

  def keepAlive(): Action[AnyContent] = Action(NoContent)

  def timeout(journeyId: String, continueUrl: RedirectUrl, origin: String): Action[AnyContent] = Action.async { implicit request =>
    emailVerificationConnector.getJourney(journeyId).map {
      case Some(journey) =>
        Ok(views.timeoutPage(controllers.routes.JourneyController.enterPasscode(journeyId, continueUrl, origin, None).url, journeyId, journey))
      case None => NotFound

    }
  }

}
