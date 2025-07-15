import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.14.0"
  private val playFrontendHmrcVersion = "12.7.0"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHmrcVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"          % bootstrapVersion % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"            % "2.19.0"         % Test,
    "org.scalacheck"               %% "scalacheck"                      % "1.18.1"         % Test,
    "org.mockito"                  %% "mockito-scala-scalatest"         % "2.0.0"          % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
