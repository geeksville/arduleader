// name := "common"

version := "0.1"

// scalaVersion := "2.10.0" // To match version used by scala-ide

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-optimise") // , "-feature"
 
// libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3"

unmanagedResourceDirectories in Compile <+= baseDirectory( _ / "src" / "main" / "scala" )

unmanagedResourceDirectories in Compile <+= baseDirectory( _ / "src" / "main" / "java" )

// libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.1.0" withSources()

// libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.1.0" withSources()

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" withSources()

