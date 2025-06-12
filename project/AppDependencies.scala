import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.0.0"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.5.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "government-gateway-test-play-30" % "6.0.0"          % Test,
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"          % bootstrapVersion % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"            % "2.17.0"         % Test,
    "org.scalacheck"               %% "scalacheck"                      % "1.17.0"         % Test,
    "org.mockito"                  %% "mockito-scala-scalatest"         % "1.17.30"        % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
