import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

lazy val microservice = Project("email-verification-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 0)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(scalaVersion := "2.12.15")
  .settings(scalacOptions ++= Seq("-Xfatal-warnings", "-feature", "-unchecked"))
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
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
  .settings(PlayKeys.playDefaultPort := 9890)
  .settings(ScalariformSettings())
  .settings(ScoverageSettings())
  .settings(SilencerSettings())
  .settings(routesImport := Seq(
    "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
    "controllers.Assets.Asset"
  ))