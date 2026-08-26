package acmebuild

import mill._
import mill.scalalib._

trait Junit5 extends TestModule.Junit5 { self: JavaModule =>
  override def mvnDeps: T[Seq[Dep]] = super.mvnDeps() ++ Seq(
    Deps.junitJupiter,
    //  )
    //  override def runMvnDeps: T[Loose.Seq[Dep]] = super.runMvnDeps() ++ Seq(
    Deps.junitPlatformReporting
  )
  abstract override def forkArgs: T[Seq[String]] = super.forkArgs() ++ Seq(
    "-Djunit.platform.reporting.open.xml.enabled=false",
    s"-Djunit.platform.reporting.output.dir=${mill.api.BuildCtx.workspaceRoot / "test-output" / "junitreports"}"
  )
}
