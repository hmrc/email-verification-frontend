import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.7"
ThisBuild / scalafmtOnCompile := true
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

lazy val microservice = Project("email-verification-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=views/.*html.*&msg=unused:s",
      "-Wconf:src=config/.*Module.*&msg=unused:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text"
    ),
    routesImport := Seq(
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
      "controllers.Assets.Asset"
    ),
    PlayKeys.playDefaultPort := 9890
  )
  .settings(ScoverageSettings())

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)
