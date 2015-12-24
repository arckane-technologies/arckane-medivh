name := """arckane-medivh"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
