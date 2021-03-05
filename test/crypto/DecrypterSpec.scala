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

package crypto

import java.util.UUID

import ch.qos.logback.classic.Level
import com.typesafe.config.ConfigFactory
import controllers.Token
import org.scalatest.LoneElement
import play.api.Configuration
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig, PlainText}
import uk.gov.hmrc.gg.test.{LogCapturing, UnitSpec}

import scala.util.Success

class DecrypterSpec extends UnitSpec with LogCapturing with LoneElement {

  "decodeAndDecryptAs" should {
    "deserialize an encoded encrypted value in to desired type" in new Setup {
      decrypter.decryptAs[Token](encryptedJson) shouldBe Success(Token(token, continueUrl))
    }

    "add a warning logging when deserialization fails" in new Setup {
      withCaptureOfLoggingFrom[Decrypter] { logs =>
        decrypter.decryptAs[Token](Crypted("foobar")).isFailure shouldBe true

        val warnLog = logs.filter(_.getLevel == Level.WARN).loneElement
        warnLog.getMessage shouldBe "Decryption failed when decrypting email verification token"
      }
    }
  }

  trait Setup {
    val configuration = new Configuration(ConfigFactory.load("application.conf"))
    val theCrypto = new CryptoWithKeysFromConfig("queryParameter.encryption", configuration.underlying)
    val continueUrl = "/continue-url"
    val token = UUID.randomUUID().toString

    val json =
      s"""
         |{
         | "token": "$token",
         | "continueUrl": "$continueUrl"
         |}
        """.stripMargin
    val encryptedJson = theCrypto.encrypt(PlainText(json))

    val decrypter = new Decrypter(theCrypto)
  }
}
