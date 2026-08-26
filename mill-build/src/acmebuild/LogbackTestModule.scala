package acmebuild

import mill.*
import mill.scalalib.*

/**
 * A TestModule, that generates a logback.xml file.
 * You need to include the [[testResources]] result into the runClasspath.
 */
trait LogbackTestModule extends JavaModule with TestModule {
  override def mvnDeps = super.mvnDeps() ++ Seq(Deps.slf4j)
  override def runMvnDeps = super.runMvnDeps() ++ Seq(Deps.logbackClassic)

  /** Place your local logback config here to (temporarily) override the default setup. */
  def logbackTestTemplate: T[Option[PathRef]] = Task.Input {
    val moduleSpec = toString()
    val templates = Seq(
      mill.api.BuildCtx.workspaceRoot / ".template" / sys.props("user.name") / moduleSpec / "logback-test.xml",
      mill.api.BuildCtx.workspaceRoot / ".template" / sys.props("user.name") / "logback-test.xml"
    )
    Task.log.debug(s"Looking for logback templates in: ${templates}")
    templates.find(os.exists).map(PathRef(_))
  }

  def logbackLevels: T[Map[String, String]] = Task { Map("de.acme" -> "debug") }

  def testLogDir = Task(persistent = true) { PathRef(Task.dest) }

  def logbackToConsole = Task.Input {
    Task.env.get("TEST_LOG_CONSOLE") match {
      case Some("0" | "no" | "false") | None => false
      case _ => true
    }
  }

  override def runClasspath: T[Seq[PathRef]] = Task { super.runClasspath() ++ Seq(testResources()) }

  //    /** Used by all tests, e.g. for logback config. */
  def testResources: Task.Simple[PathRef] = Task {
    val dest = Task.dest
    val moduleSpec = toString()
    val levels = logbackLevels()
    logbackTestTemplate() match {
      case Some(pr) =>
        os.copy.into(pr.path, dest)
      case None =>
        val logConfig =
          s"""<configuration ${if (logbackToConsole()) """debug="true"""" else ""}>
             |
             |  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
             |    <file>${testLogDir().path.toString}/test-${moduleSpec}.log</file>
             |
             |    <encoder>
             |      <pattern>%date %level [%thread] %logger{36} [%file:%line] %msg%n</pattern>
             |    </encoder>
             |  </appender>
             |
             |${if (logbackToConsole())
            """
              |  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
              |    <encoder>
              |      <pattern>%date %green(%5level) %yellow([%-20.20thread]) [%-10.10marker] %cyan(%-25.25logger{25} [%file:%line]) %msg%n</pattern>
              |    </encoder>
              |  </appender>
              |""".stripMargin
          else ""}
             |
             |  <root level="info">
             |    <appender-ref ref="FILE" />
             |${if (logbackToConsole())
            """
              |    <appender-ref ref="CONSOLE" />
              |""".stripMargin
          else ""}
             |  </root>
             |
             |${levels.map(c => s"""  <logger name="${c._1}" level="${c._2}" />""").mkString("\n")}
             |
             |</configuration>""".stripMargin
        os.write(dest / "logback-test.xml", logConfig)
    }

    PathRef(dest)
  }

}
