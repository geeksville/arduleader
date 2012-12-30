name := "scalafly"

version := "0.1"

scalaVersion := "2.9.3-RC1" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.4" withSources()
