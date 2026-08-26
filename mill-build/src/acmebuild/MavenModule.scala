package acmebuild

import mill.*
import coursier.cache.FileCache
import coursier.util.Artifact
import mill.api.{Discover, ExternalModule}

object MavenModule extends ExternalModule {

  def mavenVersion: T[String] = "3.9.16"

  // https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/
  def mavenDistributionUrl: T[String] =
    s"https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${mavenVersion()}/apache-maven-${mavenVersion()}-bin.zip"

  def mvnExe: T[PathRef] = Task {
    val cache = FileCache()
    val zip = cache.file(Artifact(mavenDistributionUrl())).run.unsafeRun()(using cache.ec)
      .fold(throw _, identity)
    os.unzip(os.Path(zip), Task.dest)
    val exe = Task.dest / s"apache-maven-${mavenVersion()}" / "bin/mvn"
    assert(os.exists(exe), s"File does not exists: ${exe}")
    println(s"perms: ${os.perms(exe)}")
    os.perms.set(exe, os.perms(exe) ++ os.PermSet.fromString("--x------"))
    println(s"perms: ${os.perms(exe)}")

    PathRef(exe).withRevalidateOnce
  }

  override def millDiscover: Discover = Discover[this.type]
}
