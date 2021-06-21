import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "5.4.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.73.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.77.0-play-27",
    "uk.gov.hmrc" %% "play-ui" % "9.1.0-play-27",
    "uk.gov.hmrc" %% "crypto" % "5.6.0",
    "uk.gov.hmrc" %% "play-language" % "4.4.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "4.3.0-play-27",
    "uk.gov.hmrc" %% "bootstrap-test-play-27" % "5.4.0" % "test,it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
