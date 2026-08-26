package acmebuild

import de.tobiasroeser.mill.jacoco.JacocoReportModule
import mill.{PathRef, T}
import mill.api.*
import mill.util.TokenReaders.given
import mill.util.Jvm

object jacoco extends ExternalModule with JacocoReportModule with DefaultTaskModule {
  override def jacocoVersion = Task.Input { Deps.jacocoVersion }

  val jacocoModules = "__:JavaModule:^TestModule:^ThirdpartyWrapper:^ProvisionModule"

  override def jacocoReportFull(evaluator: mill.api.Evaluator): Task.Command[PathRef] = Task.Command {
    jacocoReportTask(
      evaluator = evaluator,
      sources = s"${jacocoModules}.allSources",
      compiled = s"${jacocoModules}.compile",
      excludeSources = "",
      excludeCompiled = ""
    )()
  }

  // same as `super`, but does return, while `super` isn't returning.
  override def jacocoCliTask(args: Task[Seq[String]]): Task[PathRef] = Task.Anon {
    println(s"jacoco args: ${args()}")
    Jvm.callProcess(
      mainClass = "org.jacoco.cli.internal.Main",
      classPath = jacocoClasspath().map(_.path).toSeq,
      jvmArgs = forkArgs(),
      env = Map(),
      mainArgs = args(), // .map(_.replaceAll("\\Q$$REPORTDIR$$\\E", Task.dest.toIO.getAbsolutePath())),
      cwd = Task.dest
    )
    PathRef(Task.dest)
  }

  override def millDiscover: Discover = Discover[this.type]

  override def defaultTask(): String = "jacocoReportFull"
}
