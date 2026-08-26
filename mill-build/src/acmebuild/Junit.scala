package acmebuild

import mill._
import mill.scalalib._

trait Junit extends TestModule.Junit4 {
  override def mvnDeps: T[Seq[Dep]] = super.mvnDeps() ++ Seq(Deps.junit4)
}
