package acmebuild

import de.tobiasroeser.mill.jacoco.{JacocoReportModule, JacocoTestModule}
import mill.{T, Task}
import mill.api.{TaskCtx, PathRef, ModuleRef}
import mill.api.JsonFormatters.given
import mill.scalalib.{JavaModule, PublishModule}
import mill.scalalib.publish.{PomSettings, VersionControl}
import mill.javalib.testrunner.TestResult
import mill.util.{Jvm}
import os.Path

import java.io.{FileInputStream, FileOutputStream}
import java.util.jar.JarInputStream
import scala.util.chaining.scalaUtilChainingOps

/**
 * A Acme module which is build with mill (and not with mvn).
 */
trait AcmeModule
    extends JavaModule
    with PublishModule
    with AcmeCoursierModule
    with DebugModule { outer =>

  /** Override this if the project dir has a different name as the inner mill module. */
  def acmeDir: String = moduleSegments.last.value
  override def moduleDir: Path = super.moduleDir / os.up / acmeDir
  def moduleDirInfo = Task { moduleDir }

  override def artifactName: T[String] = "de.acme.cmfs." + moduleSegments.render

  override def publishVersion = Task { Acme.version() }
  override def pomSettings = Task {
    PomSettings(
      description = description,
      organization = organization,
      url = "ACME Consulting Gesellschaft mbH & Co. KG",
      licenses = Seq(),
      versionControl = VersionControl(),
      developers = Seq()
    )
  }

  def description = acmeDir
  def organization = "de.acme"

  override def jvmId = "17"

  override def manifest: T[mill.util.JarManifest] = super.manifest().add(
    "Implementation-Version" -> publishVersion()
  )

  override def javacOptions = Task {
    super.javacOptions() ++ Seq(
      "-source",
      "17",
      "-target",
      "17",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-g",
//      "-Xlint"
    )
  }

  override def javadocOptions = Task { Seq("-Xdoclint:none") }

  /**
   * Use the JAR instead of classes and resources to see the bundle manifest at runtime.
   * TODO: move into mill-osgi plugin
   */
  override def runClasspath: T[Seq[PathRef]] = Task {
    (Seq(jar()) ++ upstreamAssemblyClasspath())
  }

  override def jar: T[PathRef] = Task {
    val jar = Task.dest / s"${artifactId()}-${publishVersion()}.jar"
    os.copy(super.jar().path, jar)
    PathRef(jar)
  }

  /** Fix issue with docJar and missing annotations */
  override def docJar: T[PathRef] = Task {
    val dummyDir = Task.dest / "dummy"
    os.makeDir(dummyDir)
    PathRef(Jvm.createJar(Task.dest / "dummy.jar", Seq(dummyDir), this.manifest()))
  }

  trait AcmeModuleTests extends JavaTests with LogbackTestModule with JacocoTestModule {
    override def jacocoReportModule: ModuleRef[JacocoReportModule] = ModuleRef(jacoco)
    override def mvnDeps = Task {
      super.mvnDeps() ++ Seq(
        Deps.lambdatest,
        Deps.slf4j,
        Deps.assertJ,
        Deps.julToSlf4j
      )
    }

    override def testParallelism = true

    /**
     * This overrides [[JavaModule.transitiveLocalClasspath]], but uses the final
     * JAR files instead of just the classes directories where possible.
     * This is needed, as only the final JARs contain proper OSGi manifest entries.
     * And we also want to run some "fake" OSGi tests with Apache Felix Connect (PojoSR)
     * which requires us to "see" the OSGi bundles.
     */
    override def transitiveLocalClasspath: T[Seq[PathRef]] = Task {
      Task.traverse(recursiveModuleDeps) { m =>
        Task.Anon {
          Seq(m.jar())
        }
      }()
        .flatten
    }

    override def runClasspath: T[Seq[PathRef]] = Task {
      super.runClasspath().sortWith(classpathSorter) ++ {
        if (jacocoEnabled()) Seq(jacoco.jacocoAgentJar())
        else Seq()
      }
    }
    // TODO: remove since we no longer rely on OGSi tests
    // move the felix-connect jar on top to fix
    // Caused by: java.lang.SecurityException: class "org.osgi.framework.hooks.weaving.WovenClassListener"'s signer information does not match signer information of other classes in the same package
    def classpathSorter(l: PathRef, r: PathRef): Boolean = {
      def check(pr: PathRef): Boolean = pr.path.toIO.getPath().contains("org.apache.felix.connect")
      if (check(l)) true
      else if (check(r)) false
      else true
    }

    /** Fix issue with docJar and missing annotations */
    override def docJar: T[PathRef] = Task {
      val dummyDir = Task.dest / "dummy"
      os.makeDir(dummyDir)
      PathRef(Jvm.createJar(Task.dest / "dummy.jar", Seq(dummyDir), this.manifest()))
    }
  }

  // use special java home to run tests if Env is ACME_JAVA_HOME is present to run SWT based tests
  trait SwtTests extends AcmeModuleTests with WithSwt {
    override def forkEnv = Task {
      val m = super.forkEnv()

        val javaHome = m.get("ACME_JAVA_HOME") match {
          // Use same JVM as Mill
          case None => sys.props("java.home")
          case Some(v) => {
            println(s"using to run java home: $v");
            v
          }
        }
        sys.props.addOne("java.home" -> javaHome)
        val newEnv = m + ("JAVA_HOME" -> javaHome)
        newEnv
    }

    override def forkArgs = Task {
      super.forkArgs() ++ {
        if (BuildSettings.isMac) Seq("-XstartOnFirstThread") else Seq()
      }
    }
  }

}
