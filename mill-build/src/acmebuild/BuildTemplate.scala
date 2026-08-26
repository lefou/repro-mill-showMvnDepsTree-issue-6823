package acmebuild

import mill.api.{BuildCtx, PathRef, Result}
import mill.api.JsonFormatters.given
import mill.{Module, T, Task}

import scala.util.control.NonFatal
import scala.util.matching.Regex
import build_.package_.{app, buildsystem, cloud, framework, module, thirdparty}
import mill.util.Tasks

trait BuildTemplate extends Module {

  def listPaths(@mainargs.arg(positional = true) pathRefs: Tasks[Seq[PathRef]]) = Task.Command() {
    Task.sequence(pathRefs.value)().flatten.map(_.path).distinct.sorted
  }

}
