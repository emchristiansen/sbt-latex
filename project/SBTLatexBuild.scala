import sbt._
import Keys._

object SBTLatexBuild extends Build {
  //  def extraResolvers = Seq(
  //    resolvers ++= Seq(
  //      "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  //      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  ////      "repo.codahale.com" at "http://repo.codahale.com",
  //      "spray-io" at "http://repo.spray.io/",
  //      "typesafe-releases" at "http://repo.typesafe.com/typesafe/repo",
  //      "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"
  //    )
  //  )

  val scalaVersionString = "2.10.1"

  //  def extraLibraryDependencies = Seq(
  //    libraryDependencies ++= Seq(
  //      // "opencv" % "opencv" % "2.4.9",
  //      // "nebula" %% "nebula" % "0.1-SNAPSHOT",
  //      // "billy" %% "billy" % "0.1-SNAPSHOT",
  //      // "skunkworks" %% "skunkworks" % "0.1-SNAPSHOT",
  //      // "org.expecty" % "expecty" % "0.9",
  //      // "commons-lang" % "commons-lang" % "2.6",
  //      // "org.scala-lang" % "scala-reflect" % scalaVersionString,
  //      // "org.scala-lang" % "scala-compiler" % scalaVersionString,
  //      // "org.apache.commons" % "commons-math3" % "3.1.1",
  //      // "commons-io" % "commons-io" % "2.4",
  //      // "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
  //      // "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  //      // "org.scala-stm" %% "scala-stm" % "0.7",
  //      // "com.chuusai" %% "shapeless" % "1.2.4",
  //      // "org.clapper" %% "grizzled-scala" % "1.1.3",
  //      // "org.scalanlp" %% "breeze-math" % "0.2-SNAPSHOT",
  //      // "org.spire-math" %% "spire" % "0.3.0",
  //      // "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT",
  //      // "io.spray" %%  "spray-json" % "1.2.3",
  //      // "org.rogach" %% "scallop" % "0.8.1",
  //      // "junit" % "junit" % "4.11" % "test",
  //      // "org.imgscalr" % "imgscalr-lib" % "4.2"
  //    )
  //  )

  // TODO: Proper captialization for Latex.
  val latexSourceDirectory = TaskKey[File](
    "latex-source-directory",
    "Latex source directory")

  val latexSourceDirectoryDefinition =
    latexSourceDirectory <<= baseDirectory map { baseDirectory =>
      baseDirectory / "src" / "main" / "latex"
    }

  ////////////////////////////////

  val latexSourceFile = TaskKey[File](
    "latex-source-file",
    "Latex source file")

  val latexSourceFileDefinition =
    latexSourceFile <<= latexSourceDirectory map { latexSourceDirectory =>
      val files = (latexSourceDirectory ** "*.tex").get
      assert(
        files.size == 1,
        "There must be exactly one main .tex source. Found: " + files.toList.toString)
      files.head
    }

  /////////////////////////////////////////

  val latex = TaskKey[Unit](
    "latex",
    "Compiles latex source to PDF")

  val latexDefinition = latex <<= (latexSourceFile, cacheDirectory, target) map {
    (latexSourceFile, cacheDirectory, target) =>
      // Create the cache directory and copy the source files and dependencies
      // there.
      val latexCache = cacheDirectory / "latex"
      val sourceInCache = latexCache / latexSourceFile.getName
      IO.createDirectory(latexCache)
      IO.copyFile(latexSourceFile, sourceInCache)

      // Build the PDF.
      val pdflatex = Process(
        "pdflatex" :: "-file-line-error" :: "-halt-on-error" :: latexSourceFile.getName :: Nil,
        latexCache)
        
      val bibtex = Process(
        "bibtex" :: latexSourceFile.getName.replace(".tex", ".aux") :: Nil,
        latexCache)
   
      (pdflatex !)
      (bibtex !)
      (pdflatex !)
      (pdflatex !)

      // Copy it to the final destination.
      val pdfName = latexSourceFile.getName.replace(".tex", ".pdf")
      IO.copyFile(latexCache / pdfName, target / pdfName)
  }
  
  ///////////////////////////////////////
  
  def watchSourcesDefinition = watchSources <+= latexSourceFile map identity
  
  ///////////////////////////////////////

  def latexSettings = Seq(
    latexSourceDirectoryDefinition,
    latexSourceFileDefinition,
    latexDefinition,
    watchSourcesDefinition)
    
  lazy val root = {
    val projectName = "SBTLatex"
    val settings =
      Seq(name := projectName) ++ Project.defaultSettings ++ latexSettings  
    Project(id = projectName, base = file("."), settings = settings)
  }

  //  watchSources <++= 

  //  def updateOnDependencyChange = Seq(
  //    watchSources <++= (managedClasspath in Test) map { cp => cp.files })
  //
  //  def scalaSettings = Seq(
  //    scalaVersion := scalaVersionString,
  //    scalacOptions ++= Seq(
  //      "-optimize",
  //      "-unchecked",
  //      "-deprecation",
  //      "-feature",
  //      "-language:implicitConversions",
  //      // "-language:reflectiveCalls",
  //      "-language:postfixOps"
  //    )
  //  )

  //  def moreSettings =
  //    Project.defaultSettings ++
  //    extraResolvers ++
  //    extraLibraryDependencies ++
  //    scalaSettings ++
  //    updateOnDependencyChange

  //  val projectName = "SBTLatex"
  //  lazy val root = {
  //    val settings = moreSettings ++ Seq(name := projectName, fork := true)
  //    Project(id = projectName, base = file("."), settings = settings)
  //  }
}
