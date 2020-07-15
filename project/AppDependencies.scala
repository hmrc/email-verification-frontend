import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26"  % "1.9.0",
    "uk.gov.hmrc" %% "govuk-template"     % "5.55.0-play-26",
    "uk.gov.hmrc" %% "play-ui"            % "8.11.0-play-26",
    "uk.gov.hmrc" %% "crypto"             % "5.6.0",
    "uk.gov.hmrc" %% "play-language"      % "4.3.0-play-26"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "3.1.0",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.9.0" classifier "tests"
  )

  val overrides = {
    val jettyFromWiremockVersion = "9.4.15.v20190215"

    Seq(
      // remove these after/during GG-4751
      "org.eclipse.jetty"           % "jetty-client"       % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-continuation" % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-http"         % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-io"           % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-security"     % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-server"       % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-servlet"      % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-servlets"     % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-util"         % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-webapp"       % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty"           % "jetty-xml"          % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-api"      % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-client"   % jettyFromWiremockVersion % "it",
      "org.eclipse.jetty.websocket" % "websocket-common"   % jettyFromWiremockVersion % "it"
    )
  }

  def apply(): Seq[ModuleID] = compile ++ test
}
