name := """play-CQRS-ES"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

val akkaVersion = "2.3.8"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka"       %%  "akka-actor"      % akkaVersion,
    "com.typesafe.akka"       %%  "akka-persistence-experimental" % akkaVersion
  )
}
