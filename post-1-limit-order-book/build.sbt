name := "post-1-limit-order-book"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

resolvers += "Sonatype-Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.10.4",
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.2.2" % "test",
  "info.cukes" % "cucumber-scala_2.10" % "1.2.0" % "test",
  "info.cukes" % "cucumber-junit" % "1.2.0" % "test",
  "info.cukes" % "cucumber-java" % "1.2.0" % "test"
)

seq(cucumberSettings : _*)
seq(cucumberSettingsWithTestPhaseIntegration : _*)

cucumberFeaturesLocation := "./src/test/resources"

cucumberStepsBasePackage := "com.prystupa.matching"