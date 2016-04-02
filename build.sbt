name := "grom"
organization := "grom"
version := "0.1"
scalaVersion := "2.11.8"
parallelExecution in ThisBuild := false
publishMavenStyle := true
crossPaths := false

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked"
)


val projectMainClass = "grom.Main"

mainClass in (Compile, run) := Some(projectMainClass)
mainClass in (Compile, packageBin) := Some(projectMainClass)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12" % "test",
  "org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.7.0",
  "com.twitter.finatra" %% "finatra-http" % "2.1.5",
  "com.twitter.finatra" %% "finatra-httpclient" % "2.1.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "net.openhft" % "chronicle-map" % "3.8.0",
  "de.ruedigermoeller" % "fst" % "2.45")

