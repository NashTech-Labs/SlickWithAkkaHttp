import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys

object CommonSettings {

  lazy val projectSettings = Seq(
    scalaVersion := Dependencies.V.scala,
    resolvers += "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local",
    fork in Test := true,
    parallelExecution in Test := false,
    checksums in update := Nil
  )
}
