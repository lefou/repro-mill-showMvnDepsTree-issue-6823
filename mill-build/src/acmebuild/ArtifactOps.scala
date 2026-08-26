package acmebuild

import mill.scalalib.{BoundDep, Dep, DepSyntax}
import mill.scalalib.publish.Artifact

object ArtifactOps {
  implicit class AsDep(a: Artifact) {
    def asDep: Dep = asBoundDep.toDep
    def asBoundDep: BoundDep = BoundDep(mvn"${a.group}:${a.id}:${a.version}".dep, force = false)
  }
}