import sbt._
import Keys._

object SBTLatexBuild extends Build {
  val scalaVersionString = "2.10.1"

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

  /////////////////////////////////

  val latexUnmanagedBase = TaskKey[File](
    "latex-unmanaged-base",
    "Directory external Tex source files needed to build the PDF, e.g. *.sty, *.bst")

  val latexUnmanagedBaseDefinition =
    latexUnmanagedBase <<= unmanagedBase map identity

  //////////////////////////////////

  val latexResourceDirectory = TaskKey[File](
    "latex-resource-directory",
    "Directory containing files needed to build the PDF, e.g. *.bib, *.png")

  val latexResourceDirectoryDefinition =
    latexResourceDirectory <<= (resourceDirectory in Compile) map identity

  /////////////////////////////////////////

  val latex = TaskKey[Unit](
    "latex",
    "Compiles latex source to PDF")

  val latexDefinition = latex <<=
    (latexSourceFile, latexUnmanagedBase, latexResourceDirectory, cacheDirectory, target, streams) map {
      (latexSourceFile, latexUnmanagedBase, latexResourceDirectory, cacheDirectory, target, streams) =>
        // Create the cache directory and copy the source files and dependencies
        // there.
        val latexCache = cacheDirectory / "latex"
        IO.createDirectory(latexCache)

        // Copy the main file over.
        val sourceInCache = latexCache / latexSourceFile.getName
        IO.copyFile(latexSourceFile, sourceInCache)

        // Copy the external Tex source files.
        IO.copyDirectory(latexUnmanagedBase, latexCache)

        // Copy the extra resources needed to build.
        IO.copyDirectory(latexResourceDirectory, latexCache)

        /////////////////////////////////////////

        // Build the PDF.
        val pdflatex = Process(
          "pdflatex" :: "-file-line-error" :: "-halt-on-error" :: latexSourceFile.getName :: Nil,
          latexCache)

        val bibtex = Process(
          "bibtex" :: latexSourceFile.getName.replace(".tex", ".aux") :: Nil,
          latexCache)

        // TODO: Handle build error.
        (pdflatex !)
        (bibtex !)
        (pdflatex !)
        (pdflatex !)

        //////////////////////////////////

        // Copy it to the final destination.
        val pdfName = latexSourceFile.getName.replace(".tex", ".pdf")
        IO.copyFile(latexCache / pdfName, target / pdfName)
        streams.log.info("PDF written to %s.".format(target / pdfName))
    }

  ///////////////////////////////////////

  def watchSourcesDefinition = watchSources <++=
    (latexSourceFile, latexUnmanagedBase, latexResourceDirectory) map {
      (latexSourceFile, latexUnmanagedBase, latexResourceDirectory) =>
        Seq(
          latexSourceFile,
          latexUnmanagedBase,
          latexResourceDirectory)
    }

  ///////////////////////////////////////

  def latexSettings = Seq(
    latexSourceDirectoryDefinition,
    latexSourceFileDefinition,
    latexUnmanagedBaseDefinition,
    latexResourceDirectoryDefinition,
    latexDefinition,
    watchSourcesDefinition)

  lazy val root = {
    val projectName = "SBTLatex"
    val settings =
      Seq(name := projectName) ++ Project.defaultSettings ++ latexSettings
    Project(id = projectName, base = file("."), settings = settings)
  }
}
