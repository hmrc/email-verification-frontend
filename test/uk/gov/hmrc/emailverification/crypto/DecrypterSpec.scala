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

package uk.gov.hmrc.emailverification.crypto

import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import play.api.test.FakeApplication
import play.api.{Configuration, LoggerLike}
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig, PlainText}
import uk.gov.hmrc.emailverification.controllers.Token

import scala.util.Success

class DecrypterSpec extends Matchers with WordSpecLike {

   lazy val fakeApplication = FakeApplication(additionalConfiguration = Map(
    "queryParameter.encryption.key" -> "P5xsJ9Nt+quxGZzB3DeLfw=="
  ))

  "decodeAndDecryptAs" should {
    "deserialize an encoded encrypted value in to desired type" in new Setup {
      decrypter.decryptAs[Token](encryptedJson) shouldBe Success(Token(token, continueUrl))
    }

    "add a warning logging when deserialization fails" in new Setup {
      decrypter.decryptAs[Token](Crypted("foobar")).isFailure shouldBe true
      loggerStub.warnMessages shouldBe Seq(s"Decryption failed when decrypting email verification token")
    }
  }

  trait Setup {
    val configuration = new Configuration(ConfigFactory.load("application.conf"))
    val theCrypto = new CryptoWithKeysFromConfig("queryParameter.encryption", configuration.underlying)
    val continueUrl = "/continue-url"
    val token = UUID.randomUUID().toString

    val loggerStub = new LoggerLike {
      val warnMessages = collection.mutable.MutableList.empty[String]
      override def warn(message: => String): Unit = warnMessages += message
      override lazy val logger = ???
    }

    val json =
      s"""
         |{
         | "token": "$token",
         | "continueUrl": "$continueUrl"
         |}
        """.stripMargin
    val encryptedJson = theCrypto.encrypt(PlainText(json))

    val decrypter = new Decrypter(configuration) {
      override val crypto = theCrypto
      override val logger: LoggerLike = loggerStub
    }
  }
}
