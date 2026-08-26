package acmebuild

import de.tobiasroeser.mill.osgi.{OsgiBundleModule, OsgiHeaders}
import mill.{T, Task}
import mill.api.PathRef
import mill.scalalib.{Dep, PublishModule}

/** Build an OSGi module based on mill-osgi plugin */
trait AcmeOsgiModule extends AcmeModule with OsgiBundleModule {
  override def osgiBuildMode: OsgiBundleModule.BuildMode = OsgiBundleModule.BuildMode.ReplaceJarTarget
  override def bundleSymbolicName: T[String] = artifactName()
  override def bundleVersion: T[String] = Task { Acme.osgiVersion() }
  override def additionalHeaders: T[Map[String, String]] = Task {
    super.additionalHeaders() ++ Map("Implementation-Version" -> bundleVersion())
  }

  override def osgiHeaders: T[OsgiHeaders] = super.osgiHeaders().copy(
    // support embedded jars
    `Bundle-Classpath` = Seq(".") ++ embeddedJars().map(_.path.last)
  )

  /** We need to also include the compileModuleDeps, which is currently not supported by OsgiBundleModule. */
  override def transitiveLocalClasspath: T[Seq[PathRef]] = Task {
    Task.traverse(recursiveModuleDeps ++ compileModuleDeps) { m =>
      Task.Anon { Seq(m.jar()) }
    }()
      .flatten
  }

  def microOsgiVersionRange(v: String): String = {
    val ver = v.split("[.]", 4).toSeq
    val lower = ver.take(3).mkString(".")
    val upper = (ver.take(2) ++ ver.drop(2).headOption.map(_.toInt + 1)).mkString(".")
    s"[${lower},${upper})"
  }

  override def embeddedJars: T[Seq[PathRef]] =
    super.embeddedJars() ++ resolvedEmbeddedMvnDeps() ++ resolvedEmbeddedModuleDeps()

  def embeddedMvnDeps: T[Seq[Dep]] = Seq.empty[Dep]

  def resolvedEmbeddedMvnDeps: T[Seq[PathRef]] = Task {
    defaultResolver().classpath(embeddedMvnDeps().map(_.exclude("*" -> "*")))
  }

  def embeddedModuleDeps: Seq[PublishModule] = Seq.empty[PublishModule]

  def resolvedEmbeddedModuleDeps: T[Seq[PathRef]] = Task {
    val modsWithJar =
      Task.traverse(embeddedModuleDeps)(m => Task.Anon { (m.artifactMetadata(), m.jar()) })()
    modsWithJar.map {
      case (artifact, jar) =>
        val dest = Task.dest / s"${artifact.id}-${artifact.version}.jar"
        os.copy(jar.path, dest)
        PathRef(dest)
    }
  }

  override def explodedJars: T[Seq[PathRef]] =
    super.explodedJars() ++ resolvedExplodedMvnDeps() ++ resolvedExplodedModuleDeps()

  def explodedMvnDeps: T[Seq[Dep]] = Seq.empty[Dep]

  def resolvedExplodedMvnDeps: T[Agg[PathRef]] = Task {
    defaultResolver().classpath(explodedMvnDeps().map(_.exclude("*" -> "*")))
  }

  def explodedModuleDeps: Seq[PublishModule] = Seq.empty[PublishModule]

  def resolvedExplodedModuleDeps: T[Seq[PathRef]] = Task {
    Task.traverse(explodedModuleDeps)(m => Task.Anon { (m.jar()) })()
  }

}
