logLevel := Level.Info

//Plugin for Scalastyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

//Plugin for Scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

// this v1.5.1 is preinstalled in current IDEA releases, used for code formatting
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

//WartRemover is a flexible Scala code linting tool.
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")

//To ensures one's dependencies are fetched via coursier rather than by sbt itself
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.3")

//sbt-header is an sbt plugin for creating or updating file headers, e.g. copyright headers.
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")