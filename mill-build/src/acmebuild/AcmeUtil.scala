package acmebuild

import mill.*
import mill.api.*
import mill.scalalib._
import mill.javalib.testrunner.TestResult

import java.io.FileOutputStream
import java.net.URI
import java.nio.file.{FileSystems, Files, StandardOpenOption}
import java.util.zip.ZipEntry
import scala.jdk.CollectionConverters.*
import scala.util.Using
import java.io.InputStream
import java.io.OutputStream

trait AcmeUtil {

  def formatTestSummary(testResults: Seq[(String, Seq[TestResult])]): String = {
    val res = testResults.flatMap(_._2)
    res.groupBy(_.status).map(p => s"${p._1}: ${p._2.size}").toSeq.sorted
      .mkString(s"Tests: ${res.size}, ", ", ", "")
  }

  object zip {

    def create(
        zipFile: os.Path,
        fileEntries: Seq[(os.Path, os.SubPath)] = Seq.empty,
        contentEntries: Seq[(String, os.SubPath)] = Seq.empty,
        append: Boolean = false,
        excludes: Seq[os.SubPath] = Seq()
    ) = {

      val allEntries: Seq[(Either[os.Path, String], os.SubPath)] = contentEntries.map(e => Right(e._1) -> e._2) ++
        fileEntries.flatMap {
          case (dir, sub) if os.isDir(dir) =>
            os.walk(dir).filter(os.isFile).map(p => Left(p) -> sub / p.subRelativeTo(dir))
          case (file, sub) =>
            Seq(Left(file) -> sub)
        }
      val entries = allEntries.filter(e => excludes.forall(excl => !e._2.startsWith(excl)))
      val collisions = entries.groupBy(_._2).filter(p => p._2.size > 1)
      if (collisions.size > 1) {
        sys.error(s"Can't add more than one entry with the same name. Collisions: ${collisions}")
      }

      val baseUri = "jar:" + zipFile.toIO.getCanonicalFile.toURI.toASCIIString
      val opts: Map[String, String] = if (append) Map() else Map("create" -> "true")
      val zipFs = FileSystems.newFileSystem(URI.create(baseUri), opts.asJava)

      entries.foreach { case (fileOrText, name) =>
        val p = zipFs.getPath(name.toString()).toAbsolutePath
        writeEntry(p, fileOrText, append = false)
      }

      zipFs.close()
    }

    private def writeEntry(p: java.nio.file.Path, input: Either[os.Path, String], append: Boolean): Unit = {
      if (p.getParent != null) Files.createDirectories(p.getParent)
      val options =
        if (append) Seq(StandardOpenOption.APPEND, StandardOpenOption.CREATE)
        else Seq(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)

      val outputStream = java.nio.file.Files.newOutputStream(p, options*)
      input match {
        case Left(path) =>
          Using(os.read.inputStream(path)) { stream =>
            os.Internals.transfer(stream, outputStream, close = false)
          }
        case Right(string) =>
          outputStream.write(string.getBytes())
      }
      outputStream.close()
    }

  }

  def createZip(
      outputPath: os.Path,
      inputPaths: Seq[os.Path],
      fileFilter: (os.Path, os.RelPath) => Boolean = (p: os.Path, r: os.RelPath) => true,
      prefix: String = "",
      timestamp: Option[Long] = None,
      includeDirs: Boolean = false
  ): Unit = {
    import java.util.zip.ZipOutputStream
    import scala.collection.mutable

    os.remove.all(outputPath)
    val seen = mutable.Set.empty[os.RelPath]
    val zip = new ZipOutputStream(new FileOutputStream(outputPath.toIO))

    try {
      assert(inputPaths.iterator.forall(os.exists(_)))
      for {
        p <- inputPaths
        (file, mapping) <-
          if (os.isFile(p)) Iterator(p -> os.rel / p.last).toSeq
          else os.walk(p).filter(p => includeDirs || os.isFile(p)).map(sub => sub -> sub.relativeTo(p)).sorted
        if !seen(mapping) && fileFilter(p, mapping)
      } {
        seen.add(mapping)
        val entry = new ZipEntry(prefix + mapping.toString)
        entry.setTime(timestamp.getOrElse(os.mtime(file)))
        zip.putNextEntry(entry)
        if (os.isFile(file)) zip.write(os.read.bytes(file))
        zip.closeEntry()
      }
    } finally {
      zip.close()
    }
  }

  /**
   * Unpack the ZIP file to the given `dest` directory.
   * @param src
   * @param dest
   * @param excludes
   * @param includes if empty include all (which are not excluded)
   * @param excludes extries to exclude
   * @return The unpacked files (entry and file)
   */
  def unpackZip(
      src: os.Path,
      dest: os.Path,
      excludes: Seq[String] = Seq(),
      includes: Seq[String] = Seq()
  )(implicit ctx: TaskCtx): Seq[(String, os.Path)] = {
    var unpackedFiles: Seq[(String, os.Path)] = Seq()

    os.makeDir.all(dest)
    val byteStream = os.read.inputStream(src)
    val zipStream = new java.util.zip.ZipInputStream(byteStream)
    val includeAll = includes.isEmpty
    try {
      while ({
        zipStream.getNextEntry match {
          case null => false
          case entry =>
            val exclude = excludes.exists(ex => ex == entry.getName || entry.getName.startsWith(ex + "/"))
            val include = includeAll || includes.exists(ex => ex == entry.getName || entry.getName.startsWith(ex + "/"))
            if (include && !exclude && !entry.isDirectory) {
              val entryDest = dest / os.SubPath(entry.getName)
              os.makeDir.all(entryDest / os.up)
              val fileOut = new java.io.FileOutputStream(entryDest.toString)
              try {
                ctx.log.debug(s"Unpacking: ${entry.getName}")
                os.Internals.transfer(zipStream, fileOut, close = false)
                unpackedFiles ++= Seq(entry.getName -> entryDest)
              } finally fileOut.close()
            } else {
              ctx.log.debug(s"Skipping: ${entry.getName}")
            }
            zipStream.closeEntry()
            true
        }
      }) ()
    } finally zipStream.close()
    unpackedFiles
  }

}
object AcmeUtil extends AcmeUtil
