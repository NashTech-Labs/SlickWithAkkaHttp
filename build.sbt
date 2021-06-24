import Dependencies.{Libraries, commonModuleDependencies, dbDependencies}
import scoverage.ScoverageKeys

name := "SlickWithAkkaHttp"
version := "0.1"

Compile / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")
Compile / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

def compile(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "compile")
def test(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")

// the library is available in Bintray repository
resolvers += Resolver.bintrayRepo("dnvriend", "maven")


lazy val root = (project.in(file("."))
    settings(libraryDependencies ++= compile(commonModuleDependencies) ++  test(Libraries.scalaTest,  Libraries.mock) ++ dbDependencies++ Libraries.circe ++ test(Libraries.scalaTest, Libraries.akkaHttpTestKit,
      Libraries.mockito, Libraries.mock, Libraries.akkaStreamTestKit),
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := true
  ))
