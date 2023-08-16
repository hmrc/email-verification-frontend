import sbt.*
import play.sbt.PlayImport.ws

object AppDependencies {

  private val bootstrapVersion = "7.1.0"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.17.0-play-28"
  )

  private val test = Seq(
    "uk.gov.hmrc" %% "government-gateway-test" % "5.2.0"                % "test,it",
    "uk.gov.hmrc" %% "bootstrap-test-play-28"  % bootstrapVersion       % "test,it",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.5" % "test,it"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
