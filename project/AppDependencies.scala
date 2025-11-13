import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.19.0"
  private val playFrontendHmrcVersion = "12.20.0"
  private val playSuffix = "-play-30"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-frontend$playSuffix" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playSuffix" % playFrontendHmrcVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-test$playSuffix"      % bootstrapVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"            % "2.19.2",
    "org.scalacheck"               %% "scalacheck"                      % "1.18.1",
    "org.mockito"                  %% "mockito-scala-scalatest"         % "2.0.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
