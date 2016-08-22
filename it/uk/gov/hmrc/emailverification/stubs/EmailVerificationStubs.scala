package uk.gov.hmrc.emailverification.stubs

import com.github.tomakehurst.wiremock.client.WireMock._

object EmailVerificationStubs {
  def stubCreateVerifiedEmail(token: String, status: Int): Unit = {
    stubFor(
      post(
        urlEqualTo("/email-verification/verified-email-addresses"))
        .willReturn(aResponse()
          .withBody( s"""{"token":"$token"}""")
          .withStatus(status)))
  }

  def verifyCreateVerifiedEmail() = verify(postRequestedFor(urlEqualTo("/email-verification/verified-email-addresses")))


}
