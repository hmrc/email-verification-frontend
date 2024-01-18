import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "7.23.0"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.19.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"                  %% "government-gateway-test" % "5.2.0"          % Test,
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28"  % bootstrapVersion % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"    % "2.13.3"         % Test,
    "org.scalacheck"               %% "scalacheck"              % "1.17.0"         % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
