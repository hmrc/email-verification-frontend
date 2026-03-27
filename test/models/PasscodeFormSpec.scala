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

package models

import support.UnitSpec

class PasscodeFormSpec extends UnitSpec {

  "PasscodeForm" should {

    "bind successfully for a valid uppercase passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BCDFGH"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "bind successfully and uppercase a lowercase passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "bcdfgh"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "bind successfully and uppercase a mixed case passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BcDfGh"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "bind successfully and remove spaces from a passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BCD FGH"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "bind successfully and remove multiple spaces from a passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "B C D F G H"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "bind successfully for a lowercase passcode containing spaces" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "bcd fgh"))
      result.errors shouldBe empty
      result.value  shouldBe Some("BCDFGH")
    }

    "reject a passcode that is too short" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BCDFG"))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }

    "reject a passcode that is too long" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BCDFGHJ"))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }

    "reject a passcode containing vowels" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "ABCDFG"))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }

    "reject a passcode containing digits" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "BCD1GH"))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }

    "reject an empty passcode" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> ""))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }

    "reject a passcode consisting only of spaces" in {
      val result = PasscodeForm.form.bind(Map("passcode" -> "      "))
      result.errors.map(_.message) should contain("passcodeform.error.invalidFormat")
    }
  }
}
