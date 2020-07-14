import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

object ScalariformSettings {
  def apply() = Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignArguments, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AllowParamGroupsOnNewlines, true)
      .setPreference(DanglingCloseParenthesis, Force)
      .setPreference(FormatXml, true)
      .setPreference(NewlineAtEndOfFile, true)
      .setPreference(SpacesAroundMultiImports, false)
  )
}
