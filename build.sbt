import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

lazy val microservice = Project("email-verification-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 0)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(scalaVersion := "2.13.12")
  .settings(scalacOptions ++= Seq(
    "-Werror",
    "-Wconf:src=routes/.*&cat=unused-imports:silent",
    "-Wconf:src=views/.*html.*&cat=unused-imports:silent",
    "-Wconf:src=routes/.*&msg=Auto-application to .* is deprecated:silent",
    "-Wconf:src=routes/.*&msg=private val defaultPrefix in class Routes is never used:silent"
  ))
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
  .settings(PlayKeys.playDefaultPort := 9890)
  .settings(ScalariformSettings())
  .settings(ScoverageSettings())
  .settings(routesImport := Seq(
    "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
    "controllers.Assets.Asset"
  ))