import os.Path
import os.SubPath
import mill.javalib.Dep
import upickle.default

package object acmebuild {

  type Agg[T] = Seq[T]
  val Agg = DepSeq

  object DepSeq {
    def apply(deps: (Dep|IterableOnce[Dep])*): Seq[Dep] = deps.flatMap {
      case d: Dep => Seq(d)
      case it: IterableOnce[Dep] => it
    }
  }

  type TaskModule = mill.api.DefaultTaskModule

}
