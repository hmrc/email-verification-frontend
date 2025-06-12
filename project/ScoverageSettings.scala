import sbt.Setting
import scoverage.ScoverageKeys

object ScoverageSettings {

  val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*BuildInfo.*",
    ".*Routes.*",
    ".*RoutesPrefix.*"
  )

  def apply(): Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 77,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";")
  )

}
