import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"
//ThisBuild / Test / fork := true //Required to prevent https://github.com/sbt/sbt/issues/4609
ThisBuild / scalafmtOnCompile := true
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

lazy val microservice = Project("email-verification-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalacOptions ++= Seq(
      "-Werror",
      "-Wconf:src=routes/.*&cat=unused-imports:silent",
      "-Wconf:src=views/.*html.*&cat=unused-imports:silent",
      "-Wconf:src=routes/.*&msg=Auto-application to .* is deprecated:silent",
      "-Wconf:src=routes/.*&msg=private val defaultPrefix in class Routes is never used:silent",
      "-feature", "-deprecation",
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=html/.*:s",
    )
  )
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text"
    )
  )
  .settings(PlayKeys.playDefaultPort := 9890)
  .settings(ScoverageSettings())
  .settings(
    routesImport := Seq(
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
      "controllers.Assets.Asset"
    )
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)
