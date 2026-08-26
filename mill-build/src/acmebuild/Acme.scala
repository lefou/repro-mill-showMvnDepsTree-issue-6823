package acmebuild

//import de.tobiasroeser.mill.vcs.version.VcsVersion
import mill.util.VcsVersion
import mill.{T, Task}
import mill.api.{BuildCtx, PathRef}
import mill.api.{Discover, ExternalModule}

object Acme extends ExternalModule {

  def staticVersionFile: T[PathRef] = Task.Source(BuildCtx.workspaceRoot / "VERSION")

  def staticVersion: T[Option[String]] = Task {
    val file = staticVersionFile().path
    if (os.exists(file)) {
      val version = Option(os.read(file).trim()).filter(_.nonEmpty)
      Task.log.info(s"Using static version `${version}` from file: ${file}")
      version
    } else None
  }

  def devBranchSuffix: T[String] = Task.Input {
    val branch = os.call(("git", "branch", "--show-current")).out.text().trim() match {
      case "master" | "" => ""
      case branch => "-" + branch
    }
    branch
  }

  def gitVersion: T[String] = Task {
    val state = VcsVersion.vcsState()
    val git = state.format(dirtyHashDigits = 0, commitCountPad = 4, countSep = ".")
    if (state.lastTag.nonEmpty && state.commitsSinceLastTag == 0 && state.dirtyHash.isEmpty) {
      // don't use the suffix for tagged releases
      git
    } else {
      git + devBranchSuffix()
    }
  }

  def version: T[String] = Task {
    staticVersion().getOrElse(gitVersion())
  }
  def osgiVersion: T[String] = Task {
    val parts = version().split("[.-]", 4).toList
    val versionPart = parts.take(3).map(v => { v.toInt; v })
    val qualifierPart = parts.drop(3).map(_.replaceAll("[.]", "-"))
    (versionPart ++ qualifierPart).mkString(".")
  }

  override def millDiscover: Discover = Discover[this.type]
}
