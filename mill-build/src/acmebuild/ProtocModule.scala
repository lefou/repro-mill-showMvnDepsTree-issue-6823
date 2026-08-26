package acmebuild

import mill._
import mill.api.{PathRef, Result}
import mill.util.Jvm
import mill.scalalib._

import java.nio.file.attribute.PosixFilePermission

trait ProtocModule extends JavaModule {
  def protocSources: T[Seq[PathRef]]
  def protocVersion: T[String]
  def protocGenerated: T[Seq[PathRef]] = Task {
    val dest = Task.dest
    val dirAndFiles = protocSources()
      .map(_.path)
      .filter(os.isDir)
      .map(sourceDir => sourceDir -> os.list(sourceDir).filter(f => os.isFile(f) && f.ext == "proto"))
      .filter(_._2.nonEmpty)

    val count = dirAndFiles.foldLeft(0)((l, r) => l + r._2.size)
    println(s"Generating Java sources from ${count} proto files to ${dest.toIO.getPath()} ...")

    dirAndFiles.foreach { case (dir, files) =>
      val args = Seq(
        s"--proto_path=${dir.toIO.getPath()}",
        s"--java_out=${dest.toIO.getPath()}"
      ) ++
        (if (protocUseNativeExe) Seq() else Seq(s"-v${protocVersion()}")) ++
        files.map(_.last)
      protocWorker().run(args)
    }
    Seq(PathRef(dest))
  }
  override def generatedSources: T[Seq[PathRef]] = Task { super.generatedSources() ++ protocGenerated() }
  def protocJarMvnDeps: T[Seq[Dep]] = Seq(mvn"com.github.os72:protoc-jar:${protocVersion()}")

  def protocJarToolsClasspath: T[Seq[PathRef]] = Task { defaultResolver().classpath(protocJarMvnDeps()) }
  def protoc(args: String*) = Task.Command { protocWorker().run(args) }
  def protocUseNativeExe: Boolean = true
  def protocPlatform: T[String] = (System.getProperty("os.name"), System.getProperty("os.arch")) match {
    case ("Linux", "amd64") => Task { "linux-x86_64" }
    case ("Linux", "x86") => Task { "linux-x86_32" }
    case ("Mac OS X", "aarch64") => Task { "osx-aarch_64" }
    case ("Mac OS X", "amd64") => Task { "osx-x86_64" }
    case x => Task { Task.fail(s"Platfrom currently not supported: ${x}") }
  }
  def protocExe = Task(persistent = true) {
    // TODO: find a way to download this with coursier
    val version = protocVersion()
    val targetName = os.sub / s"protoc-${version}-${protocPlatform()}.exe"
    val target = Task.dest / targetName
    if (!os.exists(target)) {
      val url =
        s"https://repo.maven.apache.org/maven2/com/google/protobuf/protoc/${version}/${targetName}"
      val downloaded = Task.dest / s"${targetName}.part"
      os.write(downloaded, requests.get.stream(url))
      os.perms.set(downloaded, os.perms(downloaded) + PosixFilePermission.OWNER_EXECUTE)
      os.move(downloaded, target)
    }
    PathRef(target)
  }
  trait ProtocWorker {
    def run(args: Seq[String])(implicit ctx: mill.api.TaskCtx): Unit
  }
  def protocWorker =
    if (protocUseNativeExe)
      Task.Worker {
        val exe = protocExe().path
        new ProtocWorker {
          override def run(args: Seq[String])(implicit ctx: mill.api.TaskCtx): Unit = {
            ctx.log.debug(s"run: ${(exe.toString() +: args).mkString("\"", "\" \"", "\"")}")
            os.call(
              cmd = Seq(exe.toString()) ++ args,
              env = Map(),
              cwd = mill.api.BuildCtx.workspaceRoot
            )
          }
        }
      }
    else
      Task.Worker {
        import java.net.{URL, URLClassLoader}
        val cl = new URLClassLoader(protocJarToolsClasspath().map(_.path.toIO.toURI().toURL()).toArray[URL], null)
        val protocClass = cl.loadClass("com.github.os72.protocjar.Protoc")
        val runMethod = protocClass.getMethod("runProtoc", Seq(classOf[String], classOf[Array[String]])*)
        new ProtocWorker {
          override def run(args: Seq[String])(implicit ctx: mill.api.TaskCtx): Unit = {
            val exitCode = runMethod.invoke(null, Seq("protoc", args.toArray[String])*)
            if (exitCode != 0) {
              throw new RuntimeException(s"Protoc exited with exit code: ${exitCode}")
            }
          }
        }
      }
}
