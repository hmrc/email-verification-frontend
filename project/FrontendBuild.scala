import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "email-verification-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "2.0.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val frontendBootstrapVersion = "7.10.0"
  private val govukTemplateVersion = "5.0.0"
  private val playUiVersion = "5.2.0"
  private val playPartialsVersion = "5.2.0"
  private val playAuthorisedFrontendVersion = "6.2.0"
  private val playConfigVersion = "3.0.0"
  private val hmrcTestVersion = "2.2.0"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"
  private val scalaTestPlusVersion = "1.5.1"
  private val wiremockVersion = "1.58"
  private val mockitoVersion = "1.9.5"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion
  )

  abstract class TestDependencies(scope: String) {
    lazy val test = Seq(
      "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
      "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "org.jsoup" % "jsoup" % "1.8.1" % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % "it",
      "com.github.tomakehurst" % "wiremock" % wiremockVersion % "it",
      "org.mockito" % "mockito-core" % mockitoVersion % "test"
    )
  }

  object Test extends TestDependencies("test")

  object IntegrationTest extends TestDependencies("it")

  def apply() = compile ++ Test.test ++ IntegrationTest.test
}


