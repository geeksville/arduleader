import AndroidKeys._

name := "andropilot"

version := "0.1.4"

versionCode := 4

net.virtualvoid.sbt.graph.Plugin.graphSettings

platformName in Android := "android-17"  // USB host mode appeared in 3.1 (12), Ice cream sandwich and later is 80% market share, so I could drop to 15

keyalias in Android := "geeksville-android-key"

keystorePath in Android := file("andropilot/geeksville-release-key.keystore")
      
//signRelease in Android <<= signReleaseTask
      
//signRelease in Android <<= (signRelease in Android) dependsOn (packageRelease in Android)

