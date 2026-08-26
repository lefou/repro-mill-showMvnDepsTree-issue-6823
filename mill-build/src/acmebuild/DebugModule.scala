package acmebuild

import mill._
import mill.scalalib.JavaModule

/** Enable remote debugging for `run`, when `REMOTE_DEBUG` sysprop is `true`. */
trait DebugModule extends JavaModule {
  def remoteDebugEnabled = Task.Input {
    System.getProperty("REMOTE_DEBUG") == "true"
  }

  override def forkArgs = Task {
    super.forkArgs() ++ {
      // e.g. mill -D REMOTE_DEBUG=true davinci.asb.eventlog.test.test
      if (remoteDebugEnabled()) Seq("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005") else Seq()
    }
  }
}