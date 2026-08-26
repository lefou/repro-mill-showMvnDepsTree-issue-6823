package acmebuild

import mill.*
import mill.api.daemon.internal.idea.{Element, JavaFacet}
import mill.scalalib.*

/** A module that contains JPA entities and can use special JPA support in IntelliJ IDEA. */
trait IdeaJpaModule extends JavaModule {
  override def ideaJavaModuleFacets(ideaConfigVersion: Int): Task[Seq[JavaFacet]] = Task.Anon {
    super.ideaJavaModuleFacets(ideaConfigVersion)() ++ {
      ideaConfigVersion match {
        case 4 =>
          Seq(
            JavaFacet(
              `type` = "jpa",
              name = "JPA",
              config = Element(
                "configuration",
                childs = Seq(
                  Element("setting", attributes = Map("name" -> "validation-enabled", "value" -> "true")),
                  Element("setting", attributes = Map("name" -> "provider-name", "value" -> "Hibernate")),
                  Element("datasource-mapping"),
                  Element("naming-strategy-map")
                )
              )
            )
          )
        case v =>
          Task.log.error(s"Unsupported IDEA config version: ${v}")
          Seq()
      }
    }
  }
}
