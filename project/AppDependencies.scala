import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.6.0"
  private val playFrontendHmrcVersion = "12.29.0"
  private val scalatestplusVersion    = "3.2.19"
  private val playSuffix = "-play-30"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-frontend$playSuffix" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playSuffix" % playFrontendHmrcVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-test$playSuffix"      % bootstrapVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"            % "2.21.0",
    "org.scalatestplus"            %% "scalacheck-1-19"                 % s"$scalatestplusVersion.0",
    "org.scalatestplus"            %% "mockito-5-18"                    % s"$scalatestplusVersion.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
