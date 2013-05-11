import sbt._
import Keys._

object SBTLaTeX extends Plugin {
  val latexSourceDirectory = TaskKey[File](
    "latex-source-directory",
    "LaTeX source directory")

  val latexSourceDirectoryDefinition =
    latexSourceDirectory <<= baseDirectory map { baseDirectory =>
      baseDirectory / "src" / "main" / "latex"
    }

  //////////////////////////////////////////////////////////////////////////////

  val latexSourceFile = TaskKey[File](
    "latex-source-file",
    "LaTeX source file")

  val latexSourceFileDefinition =
    latexSourceFile <<= latexSourceDirectory map { latexSourceDirectory =>
      val files = (latexSourceDirectory ** "*.tex").get.filterNot(_.getPath.contains("#"))
      assert(
        files.size == 1,
        "There must be exactly one main .tex source. Found: " + files.toList.toString)
      files.head
    }

  //////////////////////////////////////////////////////////////////////////////

  val latexUnmanagedBase = TaskKey[File](
    "latex-unmanaged-base",
    "Directory containing external files needed to build the PDF, e.g. *.sty, *.bst")

  val latexUnmanagedBaseDefinition =
    latexUnmanagedBase <<= unmanagedBase map identity

  //////////////////////////////////////////////////////////////////////////////

  val latexResourceDirectory = TaskKey[File](
    "latex-resource-directory",
    "Directory containing files needed to build the PDF, e.g. *.bib, *.png")

  val latexResourceDirectoryDefinition =
    latexResourceDirectory <<= (resourceDirectory in Compile) map identity   

  //////////////////////////////////////////////////////////////////////////////

  val latex = TaskKey[Unit](
    "latex",
    "Compiles LaTeX source to PDF")

  val latexDefinition = latex <<=
    (latexSourceDirectory, latexSourceFile, latexUnmanagedBase, latexResourceDirectory, cacheDirectory, target, streams) map {
      (latexSourceDirectory, latexSourceFile, latexUnmanagedBase, latexResourceDirectory, cacheDirectory, target, streams) =>
        // Create the cache directory and copy the source files and dependencies
        // there.
        val latexCache = cacheDirectory / "latex"
        IO.createDirectory(latexCache)

        // Copy the files from the LaTeX source directory.
        IO.copyDirectory(latexSourceDirectory, latexCache)

        // Copy the main file over.
        // val sourceInCache = latexCache / latexSourceFile.getName
        // IO.copyFile(latexSourceFile, sourceInCache)

        // Copy the external Tex source files.
        IO.copyDirectory(latexUnmanagedBase, latexCache)

        // Copy the extra resources needed to build.
        IO.copyDirectory(latexResourceDirectory, latexCache)

        ////////////////////////////////////////////////////////////////////////

        // Build the PDF.
        val pdflatex = Process(
          // These flags tell pdflatex to quit if there's an error, not drop
          // into some arcane, ancient, pdflatex shell.
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

        ////////////////////////////////////////////////////////////////////////

        // Copy it to the final destination.
        val pdfName = latexSourceFile.getName.replace(".tex", ".pdf")
        IO.copyFile(latexCache / pdfName, target / pdfName)
        streams.log.info("PDF written to %s.".format(target / pdfName))
    }

  //////////////////////////////////////////////////////////////////////////////

  val watchSourcesDefinition = watchSources <++=
    (latexSourceFile, latexUnmanagedBase, latexResourceDirectory) map {
      (latexSourceFile, latexUnmanagedBase, latexResourceDirectory) =>
        Seq(
          latexSourceFile,
          latexUnmanagedBase,
          latexResourceDirectory)
    }

  //////////////////////////////////////////////////////////////////////////////
  
  override val settings = Seq(
    sbtPlugin := true,
    name := "sbt-latex",
    latexSourceDirectoryDefinition,
    latexSourceFileDefinition,
    latexUnmanagedBaseDefinition,
    latexResourceDirectoryDefinition,
    latexDefinition,
    watchSourcesDefinition)
}