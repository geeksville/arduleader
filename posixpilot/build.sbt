name := "posixpilot"

version := "0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.4" withSources()

libraryDependencies += "org.scala-lang" % "jline" % "2.10.4" withSources()

libraryDependencies += "net.java.dev.jna" % "jna" % "3.5.1" // For libFtdi

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" withSources()

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Include resources dir in eclipse classpath

EclipseKeys.withSource := true // Try to include source for libs

compileOrder := CompileOrder.JavaThenScala

