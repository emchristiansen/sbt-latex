name := "sbt-latex"

organization := "emchristiansen"

version := "0.1"

description := "SBT plugin to build LaTeX projects."

licenses := Seq("Public domain / CC0" -> 
  url("http://creativecommons.org/publicdomain/zero/1.0/"))

homepage := Some(url("https://github.com/emchristiansen/sbt-latex"))

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

