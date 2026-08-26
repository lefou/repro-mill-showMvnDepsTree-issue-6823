package acmebuild

import mill.scalalib.{Dep, ScalaModule}
import mill.{T, Task}

/** Acme module with pre-configured Scala tooling. */
trait AcmeScalaModule extends ScalaModule with AcmeModule {
  override def scalaVersion: T[String] = Deps.scalaVersion
  override def scalacOptions: T[Seq[String]] = super.scalacOptions() ++ Seq(
    "-release:17",
    "-deprecation",
    "-Xsource:3"
  )

  trait AcmeScalaTests extends ScalaTests with AcmeModuleTests with Scalatest {
    override def mvnDeps: T[Seq[Dep]] = Task {
      super.mvnDeps() ++ Seq(
        Deps.logbackClassic,
        Deps.scalatestPlusScalaCheck,
        Deps.scalaMock
      )
    }

    /** Also adds Logback Classic as SLF4J backend. */
    override def runMvnDeps: T[Seq[Dep]] = Task {
      super.runMvnDeps() ++ Seq(
        // does not work in IJ (added above)
        Deps.logbackClassic
      )
    }
  }
}
