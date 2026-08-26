package acmebuild.kotlin

import mill._
import mill.kotlinlib.KotlinModule
import mill.scalalib.{Dep, DepSyntax}

//* Kotlin Module with plugin support. */
trait ExtKotlinModule extends KotlinModule {
  override def kotlincPluginMvnDeps: T[Seq[Dep]] = super.kotlincPluginMvnDeps() ++ Seq(
    mvn"org.jetbrains.kotlin:kotlin-serialization-compiler-plugin:${kotlinVersion()}",
    mvn"org.jetbrains.kotlin:kotlin-allopen-compiler-plugin:${kotlinVersion()}"
  )
  override def kotlincOptions: T[Seq[String]] = super.kotlincOptions() ++ Seq(
    // https://kotlinlang.org/docs/all-open-plugin.html#command-line-compiler
    "-P",
    "plugin:org.jetbrains.kotlin.allopen:preset=spring"
  )
}
