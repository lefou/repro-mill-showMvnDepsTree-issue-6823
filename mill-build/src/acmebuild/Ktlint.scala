package acmebuild

import mill._
import mill.api.*
import mill.util.Jvm
import mill.scalalib._

import java.net.{URL}
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

trait Ktlint extends CoursierModule {
  def ktlintMvnDeps: T[Seq[Dep]] = Seq(
    mvn"com.pinterest:ktlint:0.42.1"
  )
  def ktlintWorkerClasspath = Task {
    defaultResolver().classpath(ktlintMvnDeps())
  }

  trait KtlintWorker {
    def run(args: Seq[String])(implicit ctx: mill.api.TaskCtx.Log): Unit
  }

  def ktlintWorkerCp: Worker[KtlintWorker] = Task.Worker {
    val cp = ktlintWorkerClasspath().map(p => p.path.toIO.toURI.toURL)
    Task.log.debug(s"Worker classpath: ${cp}")
    val cl = new URLClassLoader(cp.toArray[URL].toIndexedSeq, null)
    val mainClass = cl.loadClass("com.pinterest.ktlint.Main")
    val mainMethod = mainClass.getMethod("main", Seq[Class[?]](classOf[Array[String]])*)
    object ClKtlintWorker extends KtlintWorker {
      override def run(args: Seq[String])(using ctx: TaskCtx.Log): Unit = {
        ctx.log.debug(s"Running ktlint with args: ${args}")
        mainMethod.invoke(null, Seq(args.toArray[String])*)
      }
    }
    ClKtlintWorker
  }

  def ktlintWorkerProcess: Worker[KtlintWorker] = Task.Worker {
    object ClKtlintWorker extends KtlintWorker {
      override def run(args: Seq[String])(using ctx: TaskCtx.Log): Unit = {
        ctx.log.debug(s"Running ktlint with args: ${args}")
        Jvm.callProcess(
          mainClass = "com.pinterest.ktlint.Main",
          classPath = ktlintWorkerClasspath().iterator.map(_.path).toSeq,
          mainArgs = args
        )
      }
    }
    ClKtlintWorker
  }

  def ktlintWorker = ktlintWorkerProcess

  def ktlintHelp() = Task.Command { ktlintWorker().run(Seq("--help")) }

  def ktlintRun(args: String*) = Task.Command { ktlintWorker().run(args) }
}
