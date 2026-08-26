package acmebuild.springboot

import acmebuild.*
import mill.{T, Task, Worker}
import mill.api.PathRef
import mill.scalalib.JavaModule

trait SpringBootModule extends JavaModule {

  def springBootToolsWorker: Worker[SpringBootWorker] = Task.Worker {
    new SpringBootWorkerImpl()
  }

  /**
   * A script prepended to the resulting `springBootAssembly` to make it executable.
   * This uses the same prepend script as Mill `JavaModule` does,
   * so it supports most Linux/Unix shells (probably not `fish`)
   * as well as Windows cmd shell (the file needs a `.bat` or `.cmd` extension).
   * Set it to `""` if you don't want an executable JAR.
   */
  def springBootPrependScript: T[String] = Task {
    // we use the deprecated class, to keep compat with Mill 0.10
    mill.util.Jvm.launcherUniversalScript(
      mainClass = "org.springframework.boot.loader.JarLauncher",
      shellClassPath = Seq("$0"),
      cmdClassPath = Seq("%~dpnx0"),
      jvmArgs = forkArgs()
    )
  }

  /** The Class holding the Spring Boot Application entrypoint. By default, Spring Boot will try to auto-detect it. */
  def springBootMainClass: T[String] = Task {
    mainClass().getOrElse {
      springBootToolsWorker().findMainClass(compile().classes.path)
    }
  }

  def springBootAssembly: T[PathRef] = Task {
    val libs = runClasspath().map(_.path)
    val base = jar().path
    val mainClass = springBootMainClass()
    val dest = Task.dest / "out.jar"
    val worker = springBootToolsWorker()
    val script = springBootPrependScript()

    worker.repackageJar(
      dest = dest,
      base = base,
      mainClass = mainClass,
      libs = libs,
      assemblyScript = script
    )

    PathRef(dest)
  }

}