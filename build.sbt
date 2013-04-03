name := "sbt-latex"

organization := "emchristiansen"

version := "0.1"

description := "SBT plugin to build LaTeX projects."

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/emchristiansen/SBTLatex"))

//////////////////////////////////

sbtPlugin := true

scalacOptions := Seq("-deprecation", "-unchecked", "-optimize")

/////////////////////////////////

publishTo <<= (version) { version: String =>
   val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
   val (name, url) = if (version.contains("-SNAPSHOT"))
     ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
   else
     ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}

publishMavenStyle := false

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")

