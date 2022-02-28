import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.20.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.4.0-play-28",
    "uk.gov.hmrc" %% "crypto" % "6.1.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "4.8.0",
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.20.0" % "test,it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
