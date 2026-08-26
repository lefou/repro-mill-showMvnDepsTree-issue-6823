package acmebuild

import mill._
import mill.scalalib._

/**
 * A module that uses the zinc Java compiler to process Annotation via the APT processor.
 * This might be needed as the AspectJModule does not properly support APT processing.
 *
 * Use case: generate some immutables sources files from Java annotations.
 */
trait AptProcessorModule extends JavaModule {

  def aptOptions = Task {
    Seq(
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-proc:only"
    )
  }

  /**
   * All individual source files fed into the compiler
   */
  def allNonGeneratedSourceFiles = Task {
    def isHiddenFile(path: os.Path) = path.last.startsWith(".")

    for {
      root <- sources()
      if os.exists(root.path)
      path <- (if (os.isDir(root.path)) os.walk(root.path) else Seq(root.path))
      if os.isFile(path) && ((path.ext == "scala" || path.ext == "java") && !isHiddenFile(path))
    } yield PathRef(path)
  }

  override def generatedSources: T[Seq[PathRef]] = Task {
    super.generatedSources() ++ aptGenerateSources()
  }

  def aptGenerateSources: T[Seq[PathRef]] =    Task {
      Task.log.info("Generating Sources (apt processor) ...")
      val aptResult = jvmWorker()
        .worker()
        .compileJava(
          upstreamCompileOutput = upstreamCompileOutput(),
          sources = allNonGeneratedSourceFiles().map(_.path),
          compileClasspath = compileClasspath().map(_.path),
          javacOptions = aptOptions(),
          reporter = Task.ctx().reporter(hashCode),
          reportCachedProblems = zincReportCachedProblems(),
          incrementalCompilation = false, // this task isn't persistent anyway
          javaHome = javaHome().map(_.path)
        )
      Seq(aptResult.get.classes)
    }
}
