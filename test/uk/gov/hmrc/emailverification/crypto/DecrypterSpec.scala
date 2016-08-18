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

package uk.gov.hmrc.emailverification.crypto

import java.util.UUID

import org.joda.time.DateTime._
import org.mockito.Mockito._
import tools.MockitoSugarRush
import uk.gov.hmrc.crypto.{Crypted, PlainText, Decrypter => HmrcDecrypter}
import uk.gov.hmrc.emailverification.controllers.Token
import uk.gov.hmrc.play.test.UnitSpec

class DecrypterSpec extends UnitSpec with MockitoSugarRush {

  "decryptAs" should {
    "deserialize an encrypted value in to desired type" in new Setup {
      when(hmrcDecrypterMock.decrypt(Crypted(encryptedJson))).thenReturn(PlainText(json))
      decrypter.decryptAs[Token](encryptedJson) shouldBe Token(email, continueUrl, expiryTime)
    }
  }

  trait Setup {

    val encryptedJson = "encrypted json"
    val email = "john@doe.com"
    val continueUrl = "/continue-url"
    val expiryTime = now()

    val json =
      s"""
         |{
         | "nonce": "${UUID.randomUUID()}",
         | "email": "$email",
         | "continueUrl": "$continueUrl",
         | "expiration" : "$expiryTime"
         |}
        """.stripMargin


    val hmrcDecrypterMock: HmrcDecrypter = mock[HmrcDecrypter]
    val decrypter = new Decrypter {
      override val crypto: HmrcDecrypter = hmrcDecrypterMock
    }
  }

}
