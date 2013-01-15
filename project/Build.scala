import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._ // put this at the top of the file
import AndroidKeys._

object ScalaFlyBuild extends Build {

  // val main = "com.geeksville.shell.ScalaConsole"
  val main = "com.geeksville.flight.lead.Main"

  val assemblyCustomize = mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
    {
      // Pull all of the jansi classes from the offical dist jar, not jline
      case PathList("org", "fusesource", xs @ _*) => MergeStrategy.first
      case PathList("META-INF", "native", xs @ _*) => MergeStrategy.first
      //case "application.conf" => MergeStrategy.concat
      case ".project" => MergeStrategy.discard
      case ".classpath" => MergeStrategy.discard
      case "build.xml" => MergeStrategy.discard
      case x => old(x)
    }
  }

  lazy val root = Project(id = "root",
    base = file(".")) aggregate(posixpilot, andropilot)

  lazy val common = Project(id = "gcommon",
                           base = file("common"))

  lazy val posixpilot = Project(id = "posixpilot",
    base = file("posixpilot"),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(
      assemblyCustomize,
      mainClass in (Compile, run) := Some(main),
      mainClass in assembly := Some(main),
      
      // The three following commands are needed for my embedded REPL to work while we are inside sbt
      fork := true,
      connectInput in run := true,
      outputStrategy in run := Some(StdoutOutput)
      )) dependsOn(common)


  val proguardSettings = Seq (
    useProguard in Android := true,

    // The following packages should be excluded from android builds
    // reported in bug report: https://github.com/jberkel/android-plugin/issues/111
    proguardExclude in Android <<= (proguardExclude in Android, fullClasspath in Runtime) map { (inherited, cp_) =>
      val cp: PathFinder = cp_.files
      val excluded =
	(cp ** "logback-core*.jar") +++
        (cp ** "logback-classic*.jar") +++
        (cp ** "httpclient-*.jar") +++
        (cp ** "httpcore-*.jar") +++
        (cp ** "commons-logging-*.jar") +++
        (cp ** "commons-io-*.jar") get

      //println("inherited: " + inherited)
      //println("excluding: " + excluded)
      inherited ++ excluded
    },

      proguardOption in Android := Seq(
        // Options for all android targets
        "-dontobfuscate",

        // Options for any android app
        "@andropilot/proguard.cfg"
      ).mkString(" ")
  )

  lazy val androidAppSettings =
    Project.defaultSettings ++
    assemblySettings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings 

  lazy val androidLibrarySettings =
    Project.defaultSettings ++
    assemblySettings++
    // AndroidBase.settings ++
    AndroidProject.androidSettings  
    // TypedResources.settings

  lazy val andropilot = Project (
    "andropilot",
    file("andropilot"),
    settings = androidAppSettings
  ) dependsOn(common, scandroid, googlePlayServices)

  lazy val scandroid = Project(id = "scandroid", base = file("scandroid"), settings = androidLibrarySettings)
  lazy val googlePlayServices = Project(id = "google-play-services", base = file("google-play-services"), settings = androidLibrarySettings)

}
