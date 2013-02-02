name := "scalafly"

version := "0.1"

scalaVersion in ThisBuild := "2.10.0" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.0" withSources()

libraryDependencies += "org.scala-lang" % "jline" % "2.10.0" withSources()

libraryDependencies += "net.java.dev.jna" % "jna" % "3.5.1" // For libFtdi

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-optimise") // , "-feature"

EclipseKeys.createSrc in ThisBuild := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Include resources dir in eclipse classpath

EclipseKeys.withSource in ThisBuild := true // Try to include source for libs

EclipseKeys.relativizeLibs in ThisBuild := false // Doesn't seem to work for lib directory
