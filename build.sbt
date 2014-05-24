name := "scalafly"

version := "0.1"

scalaVersion in ThisBuild := "2.10.4" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.0" withSources()

libraryDependencies += "org.scala-lang" % "jline" % "2.10.0" withSources()

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-optimise") // , "-feature"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6") // Needed for android

EclipseKeys.createSrc in ThisBuild := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Include resources dir in eclipse classpath

EclipseKeys.withSource in ThisBuild := true // Try to include source for libs

EclipseKeys.relativizeLibs in ThisBuild := false // Doesn't seem to work for lib directory
