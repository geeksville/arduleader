import sbtandroid.AndroidKeys._

name := "andropilot"

version := "2.1.00"

versionCode := 20100

net.virtualvoid.sbt.graph.Plugin.graphSettings

// Github.settings

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "google-play-services" % "google-play-services_2.10" % "0.1-SNAPSHOT" artifacts(Artifact("google-play-services_2.10", "apklib", "apklib"))

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-optimise") // , "-feature"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6") // Needed for android

platformName in Android := "android-19"  // USB host mode appeared in 3.1 (12), Ice cream sandwich and later is 80% market share, so I could drop to 15, kitkat 4.4 is 19

dxMemory in Android := "-JXmx1024m"

keyalias in Android := "geeksville-android-key"

keystorePath in Android := file("andropilot/geeksville-release-key.keystore")
      
//signRelease in Android <<= signReleaseTask
      
//signRelease in Android <<= (signRelease in Android) dependsOn (packageRelease in Android)

//githubRepo in Android := "geeksville/arduleader"
