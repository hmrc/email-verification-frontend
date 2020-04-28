import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "email-verification-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25"  % "5.2.0",
    "uk.gov.hmrc" %% "govuk-template"     % "5.54.0-play-25",
    "uk.gov.hmrc" %% "play-ui"            % "8.8.0-play-25",
    "uk.gov.hmrc" %% "crypto"             % "5.6.0",
    "uk.gov.hmrc" %% "play-language"      % "4.2.0-play-25"
  )

  abstract class TestDependencies(scope: String)(scopeOnlyDependencies: ModuleID*) {
    lazy val dependencies: Seq[ModuleID] = Seq(
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.jsoup" % "jsoup" % "1.10.3" % scope,
      "org.pegdown" % "pegdown" % "1.6.0" % scope,
      "org.scalatest" %% "scalatest" % "3.0.1" % scope,
      "uk.gov.hmrc" %% "service-integration-test" % "0.10.0-play-25" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
    ) ++: scopeOnlyDependencies
  }

  object Test extends TestDependencies(scope = "test")(
    scopeOnlyDependencies = "org.mockito" % "mockito-core" % "2.23.4" % "test"
  )

  object IntegrationTest extends TestDependencies("it")(
    scopeOnlyDependencies =
      "com.github.tomakehurst" % "wiremock" % "2.8.0" % "it"
  )

  def apply(): Seq[ModuleID] = compile ++ Test.dependencies ++ IntegrationTest.dependencies
}
