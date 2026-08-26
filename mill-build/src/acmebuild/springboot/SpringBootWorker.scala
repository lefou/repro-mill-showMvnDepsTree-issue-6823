package acmebuild.springboot

import mill.api.TaskCtx
import os.Path

trait SpringBootWorker {
  def repackageJar(
                    dest: os.Path,
                    base: os.Path,
                    mainClass: String,
                    libs: Seq[os.Path],
                    assemblyScript: String
                  )(implicit ctx: TaskCtx): Unit

  def findMainClass(classesPath: Path): String
}