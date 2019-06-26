import Dependencies._

// ThisBuild / scalaVersion     := "2.13.0"
ThisBuild / scalaVersion     := "2.12.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "patient946_query_package",
    libraryDependencies += scalaTest % Test,
	libraryDependencies += "com.typesafe" % "config" % "1.3.4",
	libraryDependencies += "org.eclipse.rdf4j" % "rdf4j-runtime" % "2.5.2",
	libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
	libraryDependencies += "commons-logging" % "commons-logging" % "1.2"
)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-jdk14")) }

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
