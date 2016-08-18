package uk.gov.hmrc.emailverification.controllers

import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import tools.MockitoSugarRush
import uk.gov.hmrc.emailverification.crypto.{Decrypter, DecryptionError}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class EmailVerificationControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures with IntegrationPatience with MockitoSugarRush {

  "verify" should {
    "redirect to continue url if expiry time is now" in new Setup {

      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Right(Token(email, continueUrl, currentTime)))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(continueUrl)
    }

    "redirect to continue url if expiry time is in future" in new Setup {
      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Right(Token(email, continueUrl, currentTime.plusDays(1))))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(continueUrl)
    }

    "redirect to error page if expiry time is in past" in new Setup {
      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Right(Token(email, continueUrl, currentTime.minusDays(1))))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(errorUrl)
    }

    "redirect to error page if token cannot be decrypted" in new Setup {
      when(decrypterMock.decryptAs[Token](encryptedToken)).thenReturn(Left(DecryptionError))

      val result = controller.verify(encryptedToken)(request)

      status(result) shouldBe 303
      redirectLocation(result) should contain(errorUrl)
    }
  }

  trait Setup {
    val currentTime = DateTime.now
    val email = "john@doe.com"
    val continueUrl = "/continue"
    val errorUrl = "/email-verification/error"
    val encryptedToken = "some-encrypted-string"
    val decrypterMock: Decrypter = mock[Decrypter]
    val controller = new EmailVerificationController {
      override val decrypter = decrypterMock
      override lazy val dateTimeProvider = () => currentTime
    }
    implicit val request = FakeRequest()
  }


}
