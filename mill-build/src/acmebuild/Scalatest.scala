package acmebuild

import mill.*
import mill.api.Result
import mill.scalalib.*
import mill.javalib.testrunner.TestResult

trait Scalatest extends TestModule.ScalaTest {

  override def mvnDeps: T[Seq[Dep]] = super.mvnDeps() ++ Seq(Deps.scalatest)

  override protected def testTask(
      args: Task[Seq[String]],
      globSelectors: Task[Seq[String]]
  ): Task[(msg: String, results: Seq[TestResult])] = {
    val scalatestArgs = Task.Anon {
      val other = args()
      if (testFramework() == "org.scalatest.tools.Framework")
        // Only add scalatest specific config, if we actually use scalatest
        Seq("-oFG", "-u", (mill.api.BuildCtx.workspaceRoot / "test-output" / "junitreports").toString) ++ other
      else other
    }
    Task.Anon {
      super.testTask(scalatestArgs, globSelectors)()
    }
  }
}
