import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val squeryl = "org.squeryl" %% "squeryl" % "0.9.5_7"
val mysqlDriver = "mysql" % "mysql-connector-java" % "5.1.24"
val osLib = "com.lihaoyi" %% "os-lib" % "0.2.7"

lazy val root = (project in file("."))
  .settings(
    name := "dir2db",
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(
      mysqlDriver,
      squeryl,
      osLib
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
