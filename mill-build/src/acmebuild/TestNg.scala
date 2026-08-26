package acmebuild

import mill._
import mill.scalalib._

trait TestNg extends TestModule.TestNg { self: JavaModule =>
  abstract override def forkArgs = super.forkArgs() ++ Seq("-Dmill.testng.printProgress=0")
  override def mvnDeps = super.mvnDeps() ++ Seq(Deps.testng)
}