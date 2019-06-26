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
	libraryDependencies += "com.typesafe" % "config" % "1.3.4"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
