import sbt.*
import play.sbt.PlayImport.ws

object AppDependencies {

  private val bootstrapVersion = "7.21.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "6.8.0-play-28",
    "uk.gov.hmrc" %% "crypto"                     % "7.3.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "5.2.0"         % "test,it",
    "uk.gov.hmrc" %% "bootstrap-test-play-28"  % bootstrapVersion % "test,it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
