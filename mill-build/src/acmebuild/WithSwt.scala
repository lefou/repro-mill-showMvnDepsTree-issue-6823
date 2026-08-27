package acmebuild

import mill.{Task}
import mill.scalalib.JavaModule

trait WithSwt extends JavaModule {
//  override def compileMvnDeps = Task { super.compileMvnDeps() ++ Seq(Deps.eclipse.swt_platform) }
//  override def runMvnDeps = Task { super.runMvnDeps() ++ Seq(Deps.eclipse.swt_platform) }
}
