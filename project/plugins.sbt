resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.gseitz"  % "sbt-release"        % "1.0.12")
addSbtPlugin("com.typesafe.play"  % "sbt-plugin"         % "2.8.20" exclude ("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables" % "2.4.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"      % "2.0.9")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"       % "2.4.6")
addSbtPlugin("uk.gov.hmrc"        % "sbt-auto-build"     % "3.18.0")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"        % "1.5.2")
