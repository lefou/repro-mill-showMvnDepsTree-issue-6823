package acmebuild.kotlin

import mill._
import mill.scalalib._

trait KotlinDataFrameSymbolProcessingPlugin extends ExtKotlinModule { outer =>
  def kotlinDataFrameSymbolProcessingPluginDep: T[Seq[Dep]]

  override def kotlincPluginMvnDeps: T[Seq[Dep]] =
    (super.kotlincPluginMvnDeps() ++ kotlinDataFrameSymbolProcessingPluginDep()).distinct

}
