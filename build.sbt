import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val scala2_13 = "2.13.12"
val bootstrapVersion = "7.23.0"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13
//ThisBuild / Test / fork := true //Required to prevent https://github.com/sbt/sbt/issues/4609
ThisBuild / scalafmtOnCompile := true


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
      "-Wconf:src=routes/.*&msg=private val defaultPrefix in class Routes is never used:silent"
    )
  )
  .settings(
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
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
  .settings(
    resolvers ++= Seq(
      Resolver.jcenterRepo
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
