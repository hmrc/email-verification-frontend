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

package crypto

import javax.inject.{Inject, Named, Singleton}
import play.api.Logging
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.crypto.{Crypted, Encrypter}

import scala.util.{Failure, Try}

@Singleton
class Decrypter @Inject() (@Named("tokenEncryption") crypto: Encrypter with uk.gov.hmrc.crypto.Decrypter) extends Logging {

  def decryptAs[T](crypted: Crypted)(implicit reads: Reads[T]): Try[T] = Try(Json.parse(crypto.decrypt(crypted).value).as).recoverWith {
    case e: SecurityException =>
      logger.warn("Decryption failed when decrypting email verification token")
      Failure(e)
  }
}
