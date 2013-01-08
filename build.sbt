name := "scalafly"

version := "0.1"

scalaVersion := "2.9.3-RC1" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
net.virtualvoid.sbt.graph.Plugin.graphSettings
 
libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.4" withSources()

libraryDependencies += "com.typesafe.akka" % "akka-slf4j" % "2.0.4" withSources()

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" withSources()

// libraryDependencies += "org.fusesource.jansi" % "jansi" % "1.5" // No longer needed?

// libraryDependencies += "org.rxtx" % "rxtx" % "2.2pre1"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.9.3-RC1" withSources()

libraryDependencies += "org.scala-lang" % "jline" % "2.9.3-RC1" withSources()

libraryDependencies += "net.java.dev.jna" % "jna" % "3.5.1" // For libFtdi

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Include resources dir in eclipse classpath

EclipseKeys.withSource := true // Try to include source for libs
