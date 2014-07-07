// name := "common"

version := "0.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-optimise") // , "-feature"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4-SNAPSHOT"

unmanagedResourceDirectories in Compile <+= baseDirectory( _ / "src" / "main" / "scala" )

unmanagedResourceDirectories in Compile <+= baseDirectory( _ / "src" / "main" / "java" )

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" withSources()

libraryDependencies += "net.sandrogrzicic" %% "scalabuff-runtime" % "1.3.7" withSources()

libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.5.0" withSources()

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
