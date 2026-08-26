package acmebuild

import mill.{PathRef, T}
import mill.scalalib.Dep
import acmebuild.kotlin.MyKotlinModule
import mill.Task
import mill.util.Jvm

// The `extends ScalaModule` is currently needed to resolve Scala deps
trait AcmeKotlinModule extends AcmeModule with MyKotlinModule {
  override def kotlinVersion: T[String] = Deps.kotlin.kotlinVersion
  override def kotlinLanguageVersion: T[String] = "2.1"
  override def kotlinApiVersion: T[String] = "2.1"

  override def kotlincOptions = super.kotlincOptions() ++ Seq(
    "-jvm-target",
    "17"
    // Looks unused
    // "-Xcontext-receivers"
  )
  override def kotlinDataFrameSymbolProcessingPluginDep: T[Seq[Dep]] = Deps.kotlin.dataFrameAnnoProc.toSeq

  /** Fix issue with docJar and missing annotations */
  override def docJar: T[PathRef] = Task {
    val jar = Task.dest / "dummy.jar"
    Jvm.createJar(jar = jar, inputPaths = Seq(), manifest = this.manifest(), fileFilter = (_, _) => true)
    PathRef(jar)
  }

  trait AcmeKotlinTests extends AcmeModuleTests with MyKotlinModuleTests {
    override def kotlinDataFrameSymbolProcessingPluginDep: T[Seq[Dep]] = Deps.kotlin.dataFrameAnnoProc.toSeq
  }
}
