package acmebuild

import mill._
import mill.scalalib._
import acmebuild.eclipse.Archives

import build_.package_.thirdparty

//noinspection TypeAnnotation,RedundantBlock,SpellCheckingInspection
trait Deps {

  // Some Eclipse archived bundle download magic
//  import Archives._

  lazy val logbackVersion = "1.6.3"
  lazy val logstashLogbackEncoderVersion = "8.1"
  val log4j2Version = "2.26.1"
  lazy val slf4jVersion = "2.0.18"
  val scalaVersion = "2.13.18"
  def scalaBinVersion = scalaVersion.split("[.]").take(2).mkString(".")
  val swtVersion = "3.129.0.v20250221-1734"
  val slickVersion = "3.4.1"

  trait Pekko {
    // https://pekko.apache.org/docs/pekko/current/release-notes/index.html
    // Pekko 1.2 braucht protobuf 4.32
    val pekkoVersion = "1.1.5"
    val pekkoPersistenceJdbcVersion = "1.0.0"

    def pekko(name: String, version: String = pekkoVersion): Dep =
      mvn"org.apache.pekko::pekko${if (name.nonEmpty) s"-${name}" else ""}:${version}"
    val stream = pekko("stream")
    val actor = pekko("actor")
    val streamTestKit = pekko("stream-testkit")
    val persistenceJdbc = pekko("persistence-jdbc", pekkoPersistenceJdbcVersion)
    val persistenceQuery = pekko("persistence-query")
    val persistenceTestkit = pekko("persistence-testkit")
    val serializationJackson = pekko("serialization-jackson")

    val serializationJacksonDeps = Seq[Dep](
      serializationJackson.exclude("*" -> "*"),
      jacksonDatabind,
      jacksonDataformatCbor,
      jacksonDataformatYaml, // transitive dep of hilla-parser-jvm-core and swagger-core-jakarta
      jacksonDataTypeJdk8,
      jacksonDataTypeJsr310,
      jacksonParameterNames,
      jacksonScala
    )

    object http {
      // https://pekko.apache.org/docs/pekko-http/current/release-notes/index.html
      val version = "1.3.0"
      def http(name: String) = mvn"org.apache.pekko::pekko-http${if (name.nonEmpty) s"-${name}" else ""}:${version}"

      val core = http("")
      val jackson = http("jackson")
      val sprayJson = http("spray-json")
      val testkit = http("testkit")
      val cors = http("cors")
    }

    object typed {
      val actor = pekko("actor-typed")
      val actorTestkit = pekko("actor-testkit-typed")
      val persistence = pekko("persistence-typed")
      val stream = pekko("stream-typed")
    }

    object connectors {
      val file = pekko("connectors-file", version = "1.0.2")
    }
  }

  object pekko extends Pekko
  val antlr = mvn"antlr:antlr:2.7.7"
  val antContrib = mvn"ant-contrib:ant-contrib:1.0b3"
  val aopalliance = mvn"aopalliance:aopalliance:1.0"
  val asm3 = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.asm:3.3.1_1"
  val asm9Version = "9.10.1"
  val asm9 = mvn"org.ow2.asm:asm:${asm9Version}"
  //  val asm9Analysis = mvn"org.ow2.asm:asm-analysis:${asm9Version}"
  val asm9Commons = mvn"org.ow2.asm:asm-commons:${asm9Version}"
  //  val asm9Tree = mvn"org.ow2.asm:asm-tree:${asm9Version}"

  val assertJ = mvn"org.assertj:assertj-core:3.27.7"
  val assertJGuava = mvn"org.assertj:assertj-guava:${assertJ.version}"
  val assertJJodaTime = mvn"org.assertj:assertj-joda-time:2.2.0"
  val assertJDb = mvn"org.assertj:assertj-db:3.0.2"
  val asyncHttpClient = mvn"org.asynchttpclient:async-http-client:2.16.1"
  val auth0Jwt = mvn"com.auth0:java-jwt:3.19.4"

  val byteBuddy = mvn"net.bytebuddy:byte-buddy:1.18.12"

  val caffeine = mvn"com.github.ben-manes.caffeine:caffeine::3.2.4"

  trait Cats {
    val catsVersion = "2.13.0"
    // https://github.com/circe/circe/releases
    val circeVersion = "0.14.16"
    val kernel = mvn"org.typelevel::cats-kernel:${catsVersion}"
    val core = mvn"org.typelevel::cats-core:${catsVersion}"
    val circeCore = mvn"io.circe::circe-core:${circeVersion}"
    val circeGeneric = mvn"io.circe::circe-generic:${circeVersion}"
    val circeLiteral = mvn"io.circe::circe-literal:${circeVersion}"
    val circeNumbers = mvn"io.circe::circe-numbers:${circeVersion}"
    val circeJawn = mvn"io.circe::circe-jawn:${circeVersion}"
    val circeParser = mvn"io.circe::circe-parser:${circeVersion}"
    val circeValidation = mvn"io.tabmo::circe-validation-core:0.1.1"
    val circeValidationRules = mvn"io.tabmo::circe-validation-extra-rules:0.1.1"
    // https://github.com/typelevel/jawn/releases
    val jawnVersion = "1.5.1"
    val jawnParser = mvn"org.typelevel::jawn-parser:${jawnVersion}"
    val jawnAst = mvn"org.typelevel::jawn-ast:${jawnVersion}"

    val circeDeps = Seq(
      kernel,
      core,
      circeCore,
      circeGeneric,
      circeJawn,
      circeParser,
      circeNumbers,
      jawnParser,
      jawnAst,
      circeLiteral,
      circeValidation,
      circeValidationRules
    ).map(_.exclude("*" -> "*"))
  }

  object cats extends Cats
  val cglib = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.cglib:2.2.2_1"
  // dependency of easymock
  val cglibNodep = mvn"cglib:cglib-nodep:2.2.2"
  val classgraph = mvn"io.github.classgraph:classgraph:4.8.192"
  val classmate = mvn"com.fasterxml:classmate:1.7.3"
  val classmate_c = mvn"com.fasterxml:classmate:[1.7.0,)"
  val cmdOption = mvn"de.tototec:de.tototec.cmdoption:0.7.1"
  val commonsBeanutils = mvn"commons-beanutils:commons-beanutils:1.9.4"
  val commonsCodec = mvn"commons-codec:commons-codec:1.22.1"
  val commonsCollections = mvn"commons-collections:commons-collections:3.2.2"
  val commonsCollections4 = mvn"org.apache.commons:commons-collections4:4.6.0"
  val commonsCompress = mvn"org.apache.commons:commons-compress:1.28.0"
  val commonsDbcp = mvn"commons-dbcp:commons-dbcp:1.4"
  val commonsDbcp2 = mvn"org.apache.commons:commons-dbcp2:2.14.0"
  val commonsFileupload = mvn"commons-fileupload:commons-fileupload:1.6.0"
  val commonsIo = mvn"commons-io:commons-io:2.22.0"
  val commonsJxpath = mvn"commons-jxpath:commons-jxpath:1.3"
  val commonsLang = mvn"commons-lang:commons-lang:2.6"
  val commonsLang3 = mvn"org.apache.commons:commons-lang3:3.20.0"
  val commonsMath3 = mvn"org.apache.commons:commons-math3:3.6.1"
  val commonsPool = mvn"commons-pool:commons-pool:1.6"
  val curvesApi = mvn"com.github.virtuald:curvesapi:1.08"

  val doobieCore = mvn"org.tpolecat::doobie-core:0.8.8"
  val doobieH2 = mvn"org.tpolecat::doobie-h2:0.8.8"
  val dominoJava = mvn"com.github.domino-osgi:domino-java:0.3.1"

  val dynatestApi = mvn"com.github.mvysny.dynatest:dynatest-api:0.25"
  val dynatestEngine = mvn"com.github.mvysny.dynatest:dynatest-engine:0.25"


  val easymock = mvn"org.easymock:easymock:5.4.0"
  // ehcache needs JAXB 2, while we also require JAXB 3 on the classpath.
  // although both JAXB verions can be simultaniously on the classpath,
  // their Maven artifacts can not, as they have the same GAVs.
  // Hence, we repackage JAXB 2 ourselves
  // Q: Why don't we use ehcache with 'jakarta' classifier, which should support jakarta instead of javax?
  // A: Looks like spring can't instantiate it properly then
  //    according to: https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-6.0-Release-Notes
  //    Spring 6: We did not replace org.springframework.cache.ehcache with an updated version,
  //    as using Ehcache through the JCache API or its new native API is preferred.
  //
  // For better support, we need to migrate to JCache API.
  // Official guide her: https://www.ehcache.org/documentation/3.10/migration-guide.html
  //
  val ehcache = mvn"org.ehcache:ehcache:3.11.1"
  lazy val _ehcache = Task.Anon {
    Seq(
      ehcache.exclude("org.glassfish.jaxb" -> "jaxb-runtime"),
//      thirdparty.jakartaXmlBind2.asDep()
    )
  }
  val equinoxGogoAdapter = mvn"org.knowhowlab.osgi.experiments.gogo:equinox-gogo-adapter:1.0.0"

  // FIXME: Wo kommt diese Version her?
  val felixScr = mvn"org.apache.felix:org.apache.felix.scr:2.1.30"
  val felixConfigadmin = mvn"org.apache.felix:org.apache.felix.configadmin:1.9.26"
  val felixConnect = mvn"de.acme.thirdparty:org.apache.felix.connect:0.2.0.91-acme"
//  val felixConnect = mvn"de.tototec:org.apache.felix.connect:0.2.1-SNAPSHOT-acme-1"
  val felixGogoRuntime = mvn"org.apache.felix:org.apache.felix.gogo.runtime:0.10.0"
  val felixGogoShell = mvn"org.apache.felix:org.apache.felix.gogo.shell:0.10.0"
  val felixGogoCommand = mvn"org.apache.felix:org.apache.felix.gogo.command:0.12.0"
  val felixMetatype = mvn"org.apache.felix:org.apache.felix.metatype:1.0.12"

  val guava = mvn"com.google.guava:guava:33.7.1-jre"
  val guava_noJsr305 = guava.exclude("*" -> "jsr305")
  val guavaFailureAccess = mvn"com.google.guava:failureaccess:1.0.3"
  // val googleCommonCollect = mvn"com.google.common:de.acme.support.osgi.com.google.common.collect:1.0.0"
  val guavaBeanprocessor = mvn"de.acme:de.acme.beanprocessor.guavabase.beanprocessor:0.0.8"
  val gson = mvn"com.google.code.gson:gson:2.14.0"
  //  val guice = mvn"com.google.inject:guice:4.2.3"
  //  val guiceMultibindings = mvn"com.google.inject.extensions:guice-multibindings:4.2.3"

  trait Hibernate_5_6 {
    def commonsAnnotations = mvn"org.hibernate.common:hibernate-commons-annotations:5.1.2.Final"
    def core = mvn"org.hibernate:hibernate-core:5.6.15.Final"
    def jcache = mvn"org.hibernate:hibernate-jcache:5.6.15.Final"
    def jakartaPersistenceApi = mvn"javax.persistence:javax.persistence-api:2.2"
    def _all = Task.Anon {
      Seq(commonsAnnotations, core, jcache, jakartaPersistenceApi)
    }
  }

  trait Hibernate_5_6_Jakarta extends Hibernate_5_6 {
    // cs resolve org.hibernate:hibernate-core-jakarta:5.6.15.Final
    //antlr:antlr:2.7.7:default
    //com.fasterxml:classmate:1.5.1:default
    //com.sun.activation:jakarta.activation:2.0.1:default
    //com.sun.istack:istack-commons-runtime:4.0.0:default
    //jakarta.activation:jakarta.activation-api:2.0.1:default
    //jakarta.persistence:jakarta.persistence-api:3.0.0:default
    //jakarta.transaction:jakarta.transaction-api:2.0.0:default
    //jakarta.xml.bind:jakarta.xml.bind-api:3.0.1:default
    //net.bytebuddy:byte-buddy:1.12.18:default
    //org.dom4j:dom4j:2.1.3:default
    //org.glassfish.jaxb:jaxb-core:3.0.0:default
    //org.glassfish.jaxb:jaxb-runtime:3.0.0:default
    //org.glassfish.jaxb:txw2:3.0.0:default
    //org.hibernate:hibernate-core-jakarta:5.6.15.Final:default
    //org.hibernate.common:hibernate-commons-annotations:5.1.2.Final:default
    //org.jboss:jandex:2.4.2.Final:default
    //org.jboss.logging:jboss-logging:3.4.3.Final:default
    override def core = mvn"org.hibernate:hibernate-core-jakarta:5.6.15.Final"
    override def jcache = mvn"org.hibernate:hibernate-jcache:5.6.15.Final;exclude=org.hibernate:hibernate-core"
    override def jakartaPersistenceApi = mvn"jakarta.persistence:jakarta.persistence-api:3.2.0"
    override def _all = Task.Anon {
      Seq(
        this.commonsAnnotations,
        this.core,
        this.jcache,
        this.jakartaPersistenceApi,
        // we package this ourselves
//        thirdparty.jakartaXmlBind2.asDep(),
//      javaxXmlBindApi,
//      javaxXmlBindImpl,
        jakartaXmlBindApi,
        jakartaXmlBindImpl
      )
    }

  }

  // https://www.h2database.com/html/changelog.html
  val h2 = mvn"com.h2database:h2:2.3.232"
  val hikariCp = mvn"com.zaxxer:HikariCP:7.1.0"
  val hamcrestAll = mvn"org.hamcrest:hamcrest-all:1.3"
  //  @deprecated object hibernate extends Hibernate_legacy
  object hibernate extends Hibernate_5_6_Jakarta
  val http4s = mvn"org.http4s::http4s-blaze-server:0.21.1"
  val http4sCirce = mvn"org.http4s::http4s-circe:0.21.1"
  val http4sDsl = mvn"org.http4s::http4s-dsl::0.21.1"
  val httpclient = DepSeq(
    // We want to use SLF4J
    mvn"org.apache.httpcomponents:httpclient:4.5.14".exclude("commons-logging" -> "commons-logging"),
    jclOverSlf4j
  )
  val httpcore = mvn"org.apache.httpcomponents:httpcore:4.4.16"

  // https://github.com/immutables/immutables/releases
  val immutables = mvn"org.immutables:value:2.12.2"

  val jakartaAnnotation = mvn"jakarta.annotation:jakarta.annotation-api:1.3.5"
  val jacksonVersion = "2.22.2"
  // implicit deps jackson-annotations, jackson-core
  val jacksonDatabind = mvn"com.fasterxml.jackson.core:jackson-databind:$jacksonVersion"
  val jacksonDataformatCbor = mvn"com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:$jacksonVersion"
  val jacksonDataformatYaml = mvn"com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion"
  val jacksonDataTypeJsr310 = mvn"com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion"
  val jacksonDataTypeJdk8 = mvn"com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion"
  val jacksonParameterNames = mvn"com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion"
  val jacksonKotlin = mvn"com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion"
  val jacksonScala = mvn"com.fasterxml.jackson.module::jackson-module-scala:$jacksonVersion"

  val jacocoVersion = "0.8.15"
  val jansi = mvn"org.fusesource.jansi:jansi:2.4.3"

  val javaxActivationApi = mvn"javax.activation:javax.activation-api:1.2.0"
  val jakartaActivationApi = mvn"jakarta.activation:jakarta.activation-api:2.0.1"
  val javaxAnnotationApi = mvn"jakarta.annotation:jakarta.annotation-api:1.3.5"
  val javaxCdiApi = mvn"javax.enterprise:cdi-api:2.0"

  /** Expression Language */
  val javaxEl = mvn"javax.el:javax.el-api:3.0.0" // javaxEl org.glassfish:jakarta.el:jar:3.0.4
  val javaxInject = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.javax-inject:1_3"
  val javaxInterceptorApi = mvn"javax.interceptor:javax.interceptor-api:1.2.2"
  val jakartaPersistenceApi = hibernate.jakartaPersistenceApi
  val javaxServletApi = mvn"javax.servlet:javax.servlet-api:3.1.0"
  val javaxTransactionApi = mvn"org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final"
  val jakartaTransactionApi = mvn"jakarta.transaction:jakarta.transaction-api:2.0.1"
  val javaxValidationApi = mvn"javax.validation:validation-api:2.0.1.Final"
  val javaxWebsocketApi = mvn"javax.websocket:javax.websocket-api:1.0"
  val javaxXml = mvn"javax.xml:javax.xml:1.3.4"

  /** jaxb - Java XML Bind. See https://github.com/jakartaee/jaxb-api/issues/263#issuecomment-1216457886 */
//  val javaxXmlBindApi = mvn"javax.xml.bind:jaxb-api:2.3.1"
//  val javaxXmlBindImpl = mvn"com.sun.xml.bind:jaxb-impl:2.3.6"
//  val javaxXmlBindImpl = mvn"com.sun.xml.bind:jaxb-impl:2.3.9"
  val jakartaXmlBindApi = mvn"jakarta.xml.bind:jakarta.xml.bind-api:3.0.1"
  val jakartaXmlBindImpl = mvn"org.glassfish.jaxb:jaxb-runtime:3.0.2"
  @deprecated()
  val jaxbCore = mvn"org.glassfish.jaxb:jaxb-core:2.3.0.1"
  @deprecated("Use javaxXmlBindImpl or jakartaXmlBindImpl")
  val jaxbRuntime = mvn"org.glassfish.jaxb:jaxb-runtime:2.3.9"
  /* stax-api see http://veithen.io/2014/10/12/stax-osgi.html */
  val javaxXmlStream = mvn"org.apache.servicemix.specs:org.apache.servicemix.specs.stax-api-1.2:2.4.0"

  val jbossJandex = mvn"org.jboss:jandex:2.4.5.Final"
  val jbossLogging = mvn"org.jboss.logging:jboss-logging:3.6.3.Final"
  val jbossVfs = mvn"org.jboss:jboss-vfs:3.2.17.Final"
  lazy val jclOverSlf4j = mvn"org.slf4j:jcl-over-slf4j:${slf4jVersion}"
  val jcommander = mvn"com.beust:jcommander:1.72"
  val jcache = mvn"javax.cache:cache-api:1.1.1"
  @deprecated("No longer needed")
  val jgettext = mvn"org.fedorahosted.tennera:jgettext:0.15.1"
  val jodatime = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.joda-time:2.3_1"
  val javaDateThreeTenExtra =
    mvn"org.threeten:threeten-extra:1.7.2" // contains LocalDateRange, Quartal and other java.time addons.
  @deprecated("Use mssqljdbc instead", "since 2021-03-17")
  val jtds = mvn"net.sourceforge.jtds:de.acme.cmfs.support.net.sourceforge.jtds:1.2.7"
  val json4sNative = mvn"org.json4s::json4s-native:3.6.7"
  val json4sJackson = mvn"org.json4s::json4s-jackson:3.6.7"
  val jsonPath = mvn"com.jayway.jsonpath:json-path:2.9.0"
  val jsonSmart = mvn"net.minidev:json-smart:2.5.2"
  val jsonSmart_c = mvn"net.minidev:json-smart:[2.5.2]"
  val jsoup = mvn"org.jsoup:jsoup:1.23.1"
  @deprecated("Don't use at all. For null-annotations use de.acme.framework.annotation", "since 2021-09-24")
  val jsr305 = mvn"com.google.code.findbugs:jsr305:3.0.2"
  lazy val julToSlf4j = mvn"org.slf4j:jul-to-slf4j:${slf4jVersion}"
  val junit3 = mvn"junit:junit:3.8.1"
  val junit4 = mvn"junit:junit:4.13.2"
  val junitJupiter = mvn"org.junit.jupiter:junit-jupiter:5.14.3"
  val junitPlatformReporting = mvn"org.junit.platform:junit-platform-reporting:1.14.3"

  trait Kotlin {
    def kotlinVersion = "2.3.21"
    def corourtinesVersion = "1.10.2"
    def serializationJsonVersion = "1.9.0"
    def dataFrameVersion = "0.15.0"

    val stdlibCommon = mvn"org.jetbrains.kotlin:kotlin-stdlib-common:${kotlinVersion}"
    val stdlibJdk7 = mvn"org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlinVersion}"
    val stdlibJdk8 = mvn"org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}"
    val stdlib = mvn"org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}"
    // https://github.com/JetBrains/java-annotations/releases
    val annotations = mvn"org.jetbrains:annotations:26.1.0"
    val serializationJson = mvn"org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${serializationJsonVersion}"
    val serializationCore = mvn"org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${serializationJsonVersion}"
    // val serializationRuntime = mvn"org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.1.0"
    val reflect = mvn"org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}"
    val coroutines = mvn"org.jetbrains.kotlinx:kotlinx-coroutines-core:${corourtinesVersion}"
    val coroutinesJdk8 = mvn"org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${corourtinesVersion}"
    val coroutinesTest = mvn"org.jetbrains.kotlinx:kotlinx-coroutines-test:${corourtinesVersion}"
    val osgiBundle = mvn"org.jetbrains.kotlin:kotlin-osgi-bundle:${kotlinVersion}"
    val dataFrame = DepSeq(
      mvn"org.jetbrains.kotlinx:dataframe:${dataFrameVersion}"
        // depends on rhino 1.7.15, which has security vuln. SRCCLR-SID-33427 and CVE-2025-66453
        .exclude(rhinoAll.organization -> "rhino")
        // Not needed, we use jcl-over-slf4j
        .exclude("commons-logging" -> "commons-logging")
        // Not needed by us
        .exclude("org.mariadb.jdbc" -> "mariadb-java-client"),
      // since rhino 1.8, rhino is release as many small jars, but we don't know which are required here
      // hence, we use the "rhino-all" dependency
      rhinoAll,
      jclOverSlf4j
    )
    val dataFrameExcel = mvn"org.jetbrains.kotlinx:dataframe-excel:${dataFrameVersion}"
    val dataFrameAnnoProc = DepSeq(
      mvn"org.jetbrains.kotlinx.dataframe:symbol-processor-all:${dataFrameVersion}"
        .exclude("commons-logging" -> "commons-logging"),
      jclOverSlf4j
    )

  }

  trait KotlinArrow {
    def version = "1.2.4"
    val core = mvn"io.arrow-kt:arrow-core:${version}"
    val coroutines = mvn"io.arrow-kt:arrow-fx-coroutines:${version}"
  }

  trait KotlinExposed {
    // https://github.com/JetBrains/Exposed/releases
    // https://github.com/JetBrains/Exposed/blob/main/CHANGELOG.md
    def version = "0.61.0"
    val core = mvn"org.jetbrains.exposed:exposed-core:${version}"
    val dao = mvn"org.jetbrains.exposed:exposed-dao:${version}"
    val jdbc = mvn"org.jetbrains.exposed:exposed-jdbc:${version}"
    // Achtung: exposed-spring-transaction zieht ab 0.44 Spring 6. Wir bleiben auf Spring 5 und
    // vendorn stattdessen einen eigenen SpringTransactionManager (siehe cloud/vaadin ...exposed.SpringTransactionManager).
    @deprecated("DON'T USE, incompatible with Spring version < 6")
    val springTransaction = mvn"org.jetbrains.exposed:spring-transaction:${version}"
    val javaTime = mvn"org.jetbrains.exposed:exposed-java-time:${version}"

    // exposed-spring-transaction bewusst NICHT enthalten (Spring-6-Zwang) -> eigener TxManager.
    val all = Agg(core, dao, jdbc, /* springTransaction, */ javaTime)
  }

  object kotlinExposed extends KotlinExposed

  trait Kotlinx {
    val htmlJvm = mvn"org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0"
  }
  object kotlinx extends Kotlinx

  // https://github.com/mvysny/karibu-testing
  // TODO: Version 2.6.0 requires Vaadin 25+
  val karibuTesting = mvn"com.github.mvysny.kaributesting:karibu-testing-v24:2.5.0"

  object kotlin extends Kotlin
  object kotlinArrow extends KotlinArrow

  trait Ktor {
    // https://mvnrepository.com/artifact/io.ktor/ktor-bom/2.3.13
    val ktorVersion = "2.3.13"
    val serverCore = mvn"io.ktor:ktor-server-core:${ktorVersion}"
    val serverNetty = mvn"io.ktor:ktor-server-netty:${ktorVersion}"
    val serverNettyJvm_ = DepSeq(
      mvn"io.ktor:ktor-server-netty-jvm:${ktorVersion}",
      // Enforce a newer version
      netty.`codec-http2`,
      netty.`transport-native-epoll`,
      netty.`transport-native-kqueue`,
      netty.`transport-rxtx`,
      netty.`transport-sctp`,
      netty.`transport-udt`,
      netty.`transport-native-unix-common`
    )
    val serverWebsockets = mvn"io.ktor:ktor-server-websockets:${ktorVersion}"
    val serverCallLogging = mvn"io.ktor:ktor-server-call-logging:${ktorVersion}"
    val serverDefaultHeaders = mvn"io.ktor:ktor-server-default-headers:${ktorVersion}"
    val serverSessions = mvn"io.ktor:ktor-server-sessions:${ktorVersion}"
    val serverContentNegotiation = mvn"io.ktor:ktor-server-content-negotiation-jvm:${ktorVersion}"
    val serverHtmlBuilderJvm = mvn"io.ktor:ktor-server-html-builder-jvm:${ktorVersion}"

    val serializationKotlinxJson = mvn"io.ktor:ktor-serialization-kotlinx-json-jvm:${ktorVersion}"
    val serverCallLoggingJvm = mvn"io.ktor:ktor-server-call-logging-jvm:${ktorVersion}"

    // -- webjars
    val serverWebJarsJvm = mvn"io.ktor:ktor-server-webjars-jvm:${ktorVersion}"
    val webjarsHtmx = mvn"org.webjars.npm:htmx.org:1.9.12"
    val webjarsPicoCss = mvn"org.webjars.npm:picocss__pico:1.5.10"

    val allHttpServerDeps = DepSeq(
      serverCore,
      serverNetty,
      serverNettyJvm_,
      serverWebsockets,
      serverCallLogging,
      serverDefaultHeaders,
      serverSessions,
      serverContentNegotiation,
      serverHtmlBuilderJvm,
      serverWebJarsJvm,
      webjarsHtmx,
      webjarsPicoCss,
      serializationKotlinxJson,
      serverCallLoggingJvm
    )

    val clientCore = mvn"io.ktor:ktor-client-core-jvm:${ktorVersion}"
    val clientJson = mvn"io.ktor:ktor-client-json-jvm:${ktorVersion}"
    val clientLogging = mvn"io.ktor:ktor-client-logging-jvm:${ktorVersion}"
    val clientCio = mvn"io.ktor:ktor-client-cio-jvm:${ktorVersion}"

    val allHttpClientDeps = Seq(
      clientCore,
      clientJson,
      clientLogging,
      clientCio
    )
  }

  object ktor extends Ktor

  // kotlin test assertion lib like assertj
  val strikt = mvn"io.strikt:strikt-core:0.34.1"
  val striktJackson = mvn"io.strikt:strikt-jackson:0.34.1"
  val mockk = mvn"io.mockk:mockk-jvm:1.13.17"

  val lambdatest = mvn"de.tototec:de.tobiasroeser.lambdatest:0.8.0"
  lazy val logbackCore = mvn"ch.qos.logback:logback-core:${logbackVersion}"
  lazy val logbackClassic = mvn"ch.qos.logback:logback-classic:${logbackVersion}"
  lazy val logstashLockbackEncoder = mvn"net.logstash.logback:logstash-logback-encoder:${logstashLogbackEncoderVersion}"
  lazy val log4jOverSlf4j = mvn"org.slf4j:log4j-over-slf4j:${slf4jVersion}"
  val log4jApi = mvn"org.apache.logging.log4j:log4j-api::${log4j2Version}"
  val log4jToSlf4j = mvn"org.apache.logging.log4j:log4j-to-slf4j:${log4j2Version}"

  // local thirdparty
//  val log4jdbc = mvn"net.sf.log4jdbc:log4jdbc-jdbc4:1.2-7fce18c"

  trait Mail {
    val jakartaMailVersion = "2.0.2"
    val jakartaMail = mvn"com.sun.mail:jakarta.mail:${jakartaMailVersion}"
    val jakartaImap = mvn"com.sun.mail:imap:${jakartaMailVersion}"
    //    val jakartaActivation = mvn"com.sun.activation:jakarta.activation:1.2.2"
    val simpleJavaMail = mvn"org.simplejavamail:simple-java-mail:7.9.1"
    val mailDeps = Seq(jakartaMail, simpleJavaMail)
  }

  object mail extends Mail
  val mavenCore = mvn"org.apache.maven:maven-core:3.5.3"
  val mavenPluginAnnotations = mvn"org.apache.maven.plugin-tools:maven-plugin-annotations:3.5.1"
  val mavenPluginApi = mvn"org.apache.maven:maven-plugin-api:3.5.3"
  val mavenPluginTestingHarness = mvn"org.apache.maven.shared:maven-plugin-testing-harness:1.1"
  val mavenEmbedder = mvn"org.apache.maven:maven-embedder:3.5.3"
  val milynAnnotation = mvn"org.milyn:org.milyn.annotation:1.3.1"
  val mockitoKotlin = mvn"org.mockito.kotlin:mockito-kotlin:4.0.0"
  val (mssqlJdbcVersion, mssqlJdbcVersionSuffix) = ("10.2.4", ".jre11")
  // https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/10.2.4.jre11/
  // https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc_auth/10.2.4.x64/
  val mssqlJdbc_orig = mvn"com.microsoft.sqlserver:mssql-jdbc:${mssqlJdbcVersion}.jre11"

  trait Netty {
    val nettyVersion = "4.2.17.Final"
    def netty()(implicit name: sourcecode.Name) = mvn"io.netty:netty-${name.value}:${nettyVersion}"

    val all = netty()
    val codec = netty()
    val `codec-http` = netty()
    val `codec-http2` = netty()
    val common = netty()
    val handler = netty()
    val `transport-native-kqueue` = netty()
    val `transport-native-epoll` = netty()
    val `transport-native-unix-common` = netty()
    val `transport-rxtx` = netty()
    val `transport-sctp` = netty()
    val `transport-udt` = netty()
  }

  object netty extends Netty
  // https://github.com/TakahikoKawasaki/nv-i18n/blob/master/CHANGES.md
  val nvI18nCountryCodes = mvn"com.neovisionaries:nv-i18n:1.29"

  val objenesis = mvn"org.objenesis:objenesis:2.6"
  val oro = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.oro:2.0.8_6"
  val osgiCompendium = mvn"org.osgi:org.osgi.compendium:5.0.0"
  val osgiCore = mvn"org.osgi:org.osgi.core:6.0.0"
  // https://github.com/oshi/oshi/releases
  val oshiCore = mvn"com.github.oshi:oshi-core:6.11.1"
  val owaspEncoder = mvn"org.owasp.encoder:encoder:1.4.0"
  val phCommons = mvn"com.helger.commons:ph-commons:10.2.5"
  val phCommons_c = mvn"com.helger.commons:ph-commons:[10.2.5,)"
  val playJson = mvn"com.typesafe.play::play-json:2.9.4"
  // protobuf java + sun.misc for protobuf osgi
  val protobufJava = mvn"com.google.protobuf:protobuf-java:4.36.0"
  val protobufSunMisc = mvn"com.diffplug.osgi:com.diffplug.osgi.extension.sun.misc:0.0.0"
  val poi = mvn"org.apache.poi:poi:5.5.1"
  val poiOoxml = mvn"org.apache.poi:poi-ooxml:${poi.version}"
  val poiOoxmlLite = mvn"org.apache.poi:poi-ooxml-lite:${poi.version}"

//    .exclude(
//      // already embedded
//      "org.apache.poi" -> "poi-ooxml-schemas",
//      "com.github.virtuald" -> "curvesapi"
//    )
  def poiTransitiveDeps = Seq(
    poi,
    poiOoxml,
    poiOoxmlLite,
    curvesApi,
    spareBitSet
  )
  val powermockApiEasymock = mvn"org.powermock:powermock-api-easymock:1.7.4"
  val powermockModuleTestng = mvn"org.powermock:powermock-module-testng:1.7.4"
  // Map HOCON (Typesafe config) to case classes
  val pureconfig = mvn"com.github.pureconfig::pureconfig:0.17.10"

  val reflections = mvn"org.reflections:reflections:0.9.12"
  // https://github.com/mozilla/rhino/blob/master/RELEASE-NOTES.md
  val rhinoAll = mvn"org.mozilla:rhino-all:1.9.1"

  trait Spring_6 {
    // https://github.com/spring-projects/spring-framework/releases
    // Blockers for Spring 7:
    // * JPA 3.2 -> Hibernate ORM 7.1/7.2 (currently 5.6-jakarta)
    // * Junit 6
    // https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes

    def springVersion: String = "6.2.19"
    def spring(module: String): Dep = mvn"org.springframework:spring-${module}:${springVersion}"

    def core = spring("core")
    def jdbc = spring("jdbc")
    def tx = spring("tx")
    def aop = spring("aop")
    def aspects = spring("aspects")
    def beans = spring("beans")
    def context = spring("context")
    def expression = spring("expression")
    def orm = spring("orm")
    def instrument = spring("instrument")
    def webmvc = spring("webmvc")
    def web: Task[Dep] = Task.Anon { spring("web") }

    def _all = Task.Anon {
      Seq(core, jdbc, tx, aop, beans, context, expression, orm, instrument)
    }
  }

  trait SpringBoot {
    def springBootVersion = "3.5.16"
    def starter = mvn"org.springframework.boot:spring-boot-starter:${springBootVersion}"
      .exclude("org.yaml" -> "snakeyaml")
    def starterWeb = mvn"org.springframework.boot:spring-boot-starter-web:${springBootVersion}"
      .exclude("org.yaml" -> "snakeyaml")
    def starterCache = mvn"org.springframework.boot:spring-boot-starter-cache:${springBootVersion}"
    def starterJetty = mvn"org.springframework.boot:spring-boot-starter-jetty:${springBootVersion}"
    def starterTomcat = mvn"org.springframework.boot:spring-boot-starter-tomcat:${springBootVersion}"
    def starterTest = mvn"org.springframework.boot:spring-boot-starter-test:${springBootVersion}"
    def starterOauth2Client = mvn"org.springframework.boot:spring-boot-starter-oauth2-client:${springBootVersion}"
  }

  val saxon8 = mvn"net.sf.saxon:saxon:8.7"
  val saxon8Dom = mvn"net.sf.saxon:saxon-dom:8.7"
  val saxon9 = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.saxon:9.9.1-6_1"
  val scalaLibrary = mvn"org.scala-lang:scala-library:${scalaVersion}"
  val scalaReflect = mvn"org.scala-lang:scala-reflect:${scalaVersion}"
  val scalaCollectionCompat = mvn"org.scala-lang.modules::scala-collection-compat:2.11.0"
  val scalatags = mvn"com.lihaoyi::scalatags:0.9.1"
  val scalatest = mvn"org.scalatest::scalatest:3.2.20"
  val scalaMock = mvn"org.scalamock::scalamock:6.2.0"
  val scalatestPlusScalaCheck = mvn"org.scalatestplus::scalacheck-1-18:3.2.19.0"
  val slick = mvn"com.typesafe.slick::slick:${slickVersion}"
  val slickHikaricp = mvn"com.typesafe.slick::slick-hikaricp:${slickVersion}"
  lazy val slf4j = mvn"org.slf4j:slf4j-api:${slf4jVersion}"
  lazy val slf4jExt = mvn"org.slf4j:slf4j-ext:${slf4jVersion}"
  // Provide an OSGi capability needed by slf4j-api 2
  val spiflyDynamicFrameworkExtension =
    mvn"org.apache.aries.spifly:org.apache.aries.spifly.dynamic.framework.extension:1.3.7"
  val spareBitSet = mvn"com.zaxxer:SparseBitSet:1.3"
  object spring extends Spring_6
  val springRuntime = spring
  val springNonOsgi = spring
  object springBoot extends SpringBoot
  // https://github.com/spring-projects/spring-security/releases
  val springSecurityVersion = "6.5.11"
  val springSecurityWeb = mvn"org.springframework.security:spring-security-web:${springSecurityVersion}"
  val springSecurityConfig = mvn"org.springframework.security:spring-security-config:${springSecurityVersion}"
  val sttpClient = mvn"com.softwaremill.sttp.client::core:2.2.10"
  val sttpClient3 = mvn"com.softwaremill.sttp.client3::core:3.8.15"

  /** transitive deps: [[sttpClientAsyncHttpClientBackend]], [[nettyHandler]], [[`netty-codec`]], [[`netty-codec-http`]] */
  val sttpClientAsyncHttpClientBackend =
    mvn"com.softwaremill.sttp.client::async-http-client-backend-future::${sttpClient.version}"
  val sttpClientHttpClientBackend = mvn"com.softwaremill.sttp.client::httpclient-backend:${sttpClient.version}"

  val testng = mvn"org.testng:testng:7.12.0"
  val tikaCore = mvn"org.apache.tika:tika-core:3.3.2"
  val ttddyyDatasourceProxy = mvn"net.ttddyy:datasource-proxy:1.10.1"
  val tomcatEmbedCore = mvn"org.apache.tomcat.embed:tomcat-embed-core:10.1.28"
  val tototecUtilsFunctional = mvn"de.tototec:de.tototec.utils.functional:2.3.0-5-22209c"
  val tototecUtilsJFaceViewer = mvn"de.tototec:de.tototec.utils.jface.viewer:0.1.1"
    .exclude("*" -> "*")
  val typesafeConfig = mvn"com.typesafe:config:1.4.9"
  // https://github.com/testcontainers/testcontainers-java/releases
  val testContainersVersion = "2.0.5"
  val testContainers = mvn"org.testcontainers:testcontainers:${testContainersVersion}"
  val testContainersJunit5 = mvn"org.testcontainers:testcontainers-junit-jupiter:${testContainersVersion}"
  val testcontainersSelenium = mvn"org.testcontainers:testcontainers-selenium:${testContainersVersion}"
  val testContainersMsSqlServer = mvn"org.testcontainers:testcontainers-mssqlserver:${testContainersVersion}"
  // https://github.com/testcontainers/testcontainers-scala/releases
  val testContainersForScalaVersion = "0.44.1"
  val testContainersScalaTest = mvn"com.dimafeng::testcontainers-scala-scalatest:${testContainersForScalaVersion}"
  val testContainersScalaMssqlServer =
    mvn"com.dimafeng::testcontainers-scala-mssqlserver:${testContainersForScalaVersion}"

  trait Vaadin {
    // https://vaadin.com/roadmap
    // https://repo1.maven.org/maven2/com/vaadin/vaadin-bom/24.10.1/
    // https://repo1.maven.org/maven2/com/vaadin/vaadin-spring-bom/24.10.1/
    def vaadinVersion = "24.10.9"
    def flowVersion = "24.10.10"
    def flowServerVersion = flowVersion
    def vaadinSpringVersion = "24.10.2"
    def vaadinSpringBootStarterVersion = vaadinVersion

    def vaadinBom = mvn"com.vaadin:vaadin-bom:${vaadinVersion}"

    def flow(name: String, version: String = flowVersion) = mvn"com.vaadin:flow-${name}:${version}"
    def vaadin(name: String, version: String = vaadinVersion) = mvn"com.vaadin:vaadin-${name}:${version}"

    def vaadinDev_ = DepSeq(
      vaadin("dev"),
      // transitive
      netty.`all`
    )

    def flowClient = flow("client")
    def flowData = flow("data")
    def flowHtmComponents = flow("html-components")
    def flowPush = flow("push")
    def flowServer = flow("server", version = flowServerVersion)
    def flowServerProductionMode = flow("server-production-mode")
    def flowDnd = flow("dnd")

    def vaadinAccordionFlow = vaadin(s"accordion-flow")
    def vaadinCore = vaadin("core")
    def vaadinLumoTheme = vaadin(s"lumo-theme")
    def vaadinMaterialTheme = vaadin(s"material-theme")

    // commerical
    def vaadinConfirmDialogFlow = vaadin("confirm-dialog-flow")
    def vaadinChartsFlow = vaadin("charts-flow")
    def vaadinBoardFlow = vaadin("board-flow")

    def testbench = vaadin("testbench")

    def testcontainersSelenium = Deps.testcontainersSelenium
    def seleniumRemoteDriver = mvn"org.seleniumhq.selenium:selenium-remote-driver:4.25.0"
    def testcontainersJunit5 = Deps.testContainersJunit5
    // Ugrade deprecated mssql container
    // see more: https://github.com/testcontainers/testcontainers-java/issues/3079#issuecomment-3850008226
    def testcontainersMsSqlServer = Deps.testContainersMsSqlServer
    def testcontainersPlaywright = mvn"io.orange-buffalo:testcontainers-playwright:0.11.13"
    // https://playwright.dev
    // https://github.com/microsoft/playwright/releases
    def playwright = mvn"com.microsoft.playwright:playwright:1.59.0"

    //    val boardFlow = vaadin("board-flow", "3.0.0")
    //    val devServer = vaadin("dev-server", flowVersion)
    def server = vaadin("server")
    def vaadinSpring = vaadin("spring", vaadinSpringVersion)
    def vaadinSpringBootStarter = vaadin("spring-boot-starter", vaadinSpringBootStarterVersion)
  }

  object vaadin extends Vaadin
  @deprecated("Currently unused")
  val vaddonMediaQuery = mvn"org.vaddon:mediaquery:4.0.1"
//  val vaadinToggle = mvn"com.vaadin.componentfactory:togglebutton:3.0.0"

  /* stax2-api */
  val woodstoxStax2Api = mvn"org.codehaus.woodstox:stax2-api:4.2.1"
  val woodstoxStax2CoreAsl = mvn"org.codehaus.woodstox:woodstox-core-asl:4.4.1"

  val ulid = mvn"de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0"
  trait LiHaoyi {
    val upickleVersion = "4.4.3"
    val ammoniteVersion = "3.0.9"

    val ammonite = mvn"com.lihaoyi:::ammonite:${ammoniteVersion}"
    val ammonite_withoutJline = ammonite.exclude(
      // already provided by jline
      "org.jline" -> "jline-reader",
      "org.jline" -> "jline-terminal",
      "org.jline" -> "jline-terminal-jna"
    )
    val ammoniteSshd = mvn"com.lihaoyi:::ammonite-sshd:${ammoniteVersion}"
//    val fansi = mvn"com.lihaoyi::fansi:0.5.0"
//    val geny = mvn"com.lihaoyi::geny:1.1.1"
    val osLib = mvn"com.lihaoyi::os-lib:0.11.8"
    val pprint = mvn"com.lihaoyi::pprint:0.9.6"
//    val sourcecode = mvn"com.lihaoyi::sourcecode:0.4.2"
    val ujson = mvn"com.lihaoyi::ujson:${upickleVersion}"
//    val upack = mvn"com.lihaoyi::upack:${upickleVersion}"
    val upickle = mvn"com.lihaoyi::upickle:${upickleVersion}"
//    val upickleCore = mvn"com.lihaoyi::upickle-core:${upickleVersion}"
//    val upickleImplicits = mvn"com.lihaoyi::upickle-implicits:${upickleVersion}"

    val pprintDeps = Seq(pprint)
    val upickleDeps = Seq(upickle)
  }

  object liHaoyi extends LiHaoyi

  val xerces = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.xerces:2.12.2_1"
  val xmlbeans = mvn"org.apache.servicemix.bundles:org.apache.servicemix.bundles.xmlbeans:5.2.0_1"
  val xmlcommons = mvn"org.apache.xmlcommons:com.springsource.org.apache.xmlcommons:1.3.3"
  val xmlpull = mvn"org.xmlpull:com.springsource.org.xmlpull:1.1.4"
  val xmlpullXmp1 = mvn"org.xmlpull:de.acme.support.osgi.org.xmlpull.xmp1:1.1.3.4-O"
  val xmlResolver = mvn"org.apache.xml:com.springsource.org.apache.xml.resolver:1.2.0"

  // https://sourceforge.net/p/yajsw/news/ and https://sourceforge.net/projects/yajsw/files/yajsw/
  val yajswVersion = "13.18"

  /** Force initialization of all relevant dependencies value to make thirdparty.rcp installation/deployment work. */
  def init(): Unit

}
object Deps extends Deps {

  implicit class DepOps(val dep: Dep) extends AnyVal {
    def nonScalaDep: Dep = dep.bindDep(Deps.scalaBinVersion, Deps.scalaVersion, "").toDep
  }

  /** Force initialization of all relevant dependencies value to make thirdparty.rcp installation/deployment work. */
  //noinspection ScalaUnusedExpression
  override def init(): Unit = {
    spring
    vaadin
    //    Deps.eclipse.e4
    //    Deps.eclipse.deps
  }
}
