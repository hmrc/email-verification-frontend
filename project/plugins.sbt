resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.18.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "1.4.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.19")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.15.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.18.0")