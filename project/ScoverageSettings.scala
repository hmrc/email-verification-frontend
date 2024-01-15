import sbt.Def
import scoverage.ScoverageKeys

object ScoverageSettings {
  def apply(): Seq[Def.Setting[? >: String & Double & Boolean]] =   Seq(
    ScoverageKeys.coverageExcludedPackages := Seq(
      "<empty>",
      "Reverse*",
      "models/.data/..*",
      "view.*",
      ".*standardError*.*",
      ".*govuk_wrapper*.*",
      ".*main_template*.*",
      "uk.gov.hmrc.BuildInfo",
      "com.kenshoo.play.metrics*",
      "testOnlyDoNotUseInAppConf.*",
      "uk.gov.hmrc",
      "uk.gov.hmrc.emailverification.controllers.javascript*",
      "app.*",
      "prod.*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
