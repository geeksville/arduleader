// name := "common"

version := "0.1"

// scalaVersion := "2.10.0" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
net.virtualvoid.sbt.graph.Plugin.graphSettings
 
libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2"

// libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.1.0" withSources()

// libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.1.0" withSources()

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" withSources()

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Include resources dir in eclipse classpath

EclipseKeys.withSource := true // Try to include source for libs

EclipseKeys.relativizeLibs := false
