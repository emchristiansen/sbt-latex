SBT plugin to build LaTeX projects with SBT! Inspired by [latex-cv](https://github.com/stuhood/latex-cv).

Features:

1. Manages the ugly intermediate TeX files so you don't have to look at them.
2. Can automatically rebuild your project whenever a file changes:
 1. Start `sbt` and run `~latex`.

To use:

1. Make sure `pdflatex` and `bibtex` are installed, as well as any LaTeX libraries you may want:
 1. In Ubuntu, you can install everything with `sudo apt-get install texlive-full`.
2. Add the command `addSbtPlugin("emchristiansen" % "sbt-latex" % "0.1")` to `project/plugins.sbt`.
3. Expected file locations:
 1. Place your main `.tex` source in `src/main/latex/`.
 2. Place resources, like `.bib` files and figures, in `src/main/resources/`.
 3. Place external dependencies, like `.sty` files, in `lib/`.

You can also use the [g8 template](https://github.com/emchristiansen/SBTLatexTemplate.g8) to auto-generate a LaTeX project.

License: Public domain / I don't care / [CC0](http://creativecommons.org/publicdomain/zero/1.0/).


