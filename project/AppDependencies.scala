import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.14.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.55.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.11.0-play-26",
    "uk.gov.hmrc" %% "crypto" % "5.6.0",
    "uk.gov.hmrc" %% "play-language" % "4.3.0-play-26"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "3.2.0",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.9.0" classifier "tests"
  )


  def apply(): Seq[ModuleID] = compile ++ test
}
