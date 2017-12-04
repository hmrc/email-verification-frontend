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
    "uk.gov.hmrc" %% "frontend-bootstrap" % "8.12.0"
  )

  abstract class TestDependencies(scope: String)(scopeOnlyDependencies: ModuleID*) {
    lazy val dependencies: Seq[ModuleID] = Seq(
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.jsoup" % "jsoup" % "1.10.3" % scope,
      "org.pegdown" % "pegdown" % "1.6.0" % scope,
      "org.scalatest" %% "scalatest" % "3.0.1" % scope,
      "uk.gov.hmrc" %% "hmrctest" % "2.4.0" % scope
    ) ++: scopeOnlyDependencies
  }

  object Test extends TestDependencies(scope = "test")(
    scopeOnlyDependencies = "org.mockito" % "mockito-core" % "2.6.2" % "test"
  )

  object IntegrationTest extends TestDependencies("it")(
    scopeOnlyDependencies =
      "com.github.tomakehurst" % "wiremock" % "2.8.0" % "it",
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "it"
  )

  def apply(): Seq[ModuleID] = compile ++ Test.dependencies ++ IntegrationTest.dependencies
}
