import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.14.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.83.0-play-28",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.80.0-play-28",
    "uk.gov.hmrc" %% "play-ui" % "9.7.0-play-28",
    "uk.gov.hmrc" %% "crypto" % "5.6.0",
    "uk.gov.hmrc" %% "play-language" % "5.1.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "4.6.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.14.0" % "test,it",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2" % "test,it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
