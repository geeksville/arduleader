import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._ // put this at the top of the file

object ScalaFlyBuild extends Build {

  val assemblyCustomize = mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
    {
      // Pull all of the jansi classes from the offical dist jar, not jline
      case PathList("org", "fusesource", xs @ _*) => MergeStrategy.first
      
      //case "application.conf" => MergeStrategy.concat
      case ".project" => MergeStrategy.discard
      case ".classpath" => MergeStrategy.discard
      case "build.xml" => MergeStrategy.discard
      case x => old(x)
    }
  }

  lazy val root = Project(id = "skalafly",
    base = file("."),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(assemblyCustomize))

}
