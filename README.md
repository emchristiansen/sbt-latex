Build LaTeX projects with SBT! Inspired by [latex-cv](https://github.com/stuhood/latex-cv).

This tool calls native LaTeX tools (pdflatex, bibtex), so install those first.

To use:
1. Place your main .tex file in src/main/latex/.
2. Place external library files, e.g. conference-specific style files, in lib/.
3. Place extra build dependencies, e.g. figures, in src/main/resources/.
4. Run *sbt latex* from the project root (or even *~sbt latex* to automatically rebuild when files change).

The project comes seeded with an example to get you started.

TODO:
1. Make this an SBT plugin.
2. Make a giter8 template.