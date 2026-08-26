package acmebuild.kotlin

import mill.*
import mill.api.daemon.internal.idea.{Element, IdeaConfigFile, JavaFacet}
import mill.javalib.*

trait MyKotlinModule extends ExtKotlinModule
    with KotlinDataFrameSymbolProcessingPlugin
    with GenIdeaModule {

  override def ideaConfigFiles(ideaConfigVersion: Int): Task[Seq[IdeaConfigFile]] = Task.Anon {
    super.ideaConfigFiles(ideaConfigVersion)() ++ {
      assert(ideaConfigVersion == 4)

      Seq(
        IdeaConfigFile(
          subPath = (os.sub/ "kotlinc.xml").toNIO,
          component = "Kotlin2JvmCompilerArguments",
          config = Seq(
            Element("option", Map("name" -> "jvmTarget", "value" -> "17"))
          )
        ),
        IdeaConfigFile(
          subPath = (os.sub/ "kotlinc.xml").toNIO,
          component = "Kotlin2JsCompilerArguments",
          config = Seq(
            Element("option", Map("name" -> "moduleKind", "value" -> "plain"))
          )
        ),
        IdeaConfigFile(
          subPath = (os.sub/ "kotlinc.xml").toNIO,
          component = "KotlinCommonCompilerArguments",
          config = Seq(
            Element("option", Map("name" -> "apiVersion", "value" -> kotlinApiVersion())),
            Element("option", Map("name" -> "languageVersion", "value" -> kotlinLanguageVersion()))
          )
        ),
        IdeaConfigFile(
          subPath = (os.sub/ "kotlinc.xml").toNIO,
          component = "KotlinJpsPluginSettings",
          config = Seq(
            Element("option", Map("name" -> "version", "value" -> kotlinVersion()))
          )
        )
      )

    }
  }

  override def ideaJavaModuleFacets(ideaConfigVersion: Int): Task[Seq[JavaFacet]] = Task.Anon {
    val options = allKotlincOptions()
    super.ideaJavaModuleFacets(ideaConfigVersion)() ++ {
      ideaConfigVersion match {
        case 4 =>
          Seq(
            JavaFacet(
              `type` = "kotlin-language",
              name = "Kotlin",
              config = Element(
                "configuration",
                attributes = Map(
                  "version" -> "3",
                  "platform" -> "JVM 17",
                  "allPlatforms" -> "JVM [17]",
                  "useProjectSettings" -> "false"
                ),
                childs = Seq(
                  Element(
                    "compilerSettings",
                    childs = Seq(
                      Element(
                        "option",
                        attributes = Map("name" -> "additionalArguments", "value" -> s"${options.mkString(" ")}")
                      )
                    )
                  ),
                  Element(
                    "compilerArguments",
                    childs = Seq(
                      Element("option", attributes = Map("name" -> "jvmTarget", "value" -> "17")),
                      Element(
                        "option",
                        attributes = Map("name" -> "languageVersion", "value" -> kotlinLanguageVersion())
                      ),
                      Element("option", attributes = Map("name" -> "apiVersion", "value" -> kotlinApiVersion()))
                    )
                  )
                )
              )
            )
          )
        case _ => Seq()
      }
    }
  }

  trait MyKotlinModuleTests extends super.KotlinTests with MyKotlinModule
}
