package acmebuild.eclipse

import mill.scalalib._

/**
 * Defines an Archive file (ZIP, ...) than can be downloaded from a given URL.
 * The bundle must be location inside the archive with the schematic name `${artifactId}_${version}.jar`.
 * Source bundles must have the schematic name `${artifactId}.source_${version}.jar`.
 *
 * @param url
 *               The URL of the archive
 * @param bundlePath
 *               The path inside the archive where the bundle can be found.
 * @param groupId
 *               The groupId for the bundles inside this archive
 * @param source If `true` the archive also contains a source JAR of the bundle.
 *               This information is used to install the source jar alongside the jar into the maven repository,
 *               thus it is available to your IDE.
 */
case class Archive(
    url: String,
    bundlePath: String = "plugins/",
    groupId: String,
    source: Boolean = true,
    provision: Boolean = true
) {
  import Archive._

  /**
   * Use this to specify the artifactId to create a Maven GAV
   * while also internally register this bundle for automated maven provisioning.
   */
  def apply(artifactId: String, version: String): Dep = {
    val req = ProvisionRequest(this, artifactId, version)
    Archive._bundles ++= Seq(req)
    mvn"${groupId}:${artifactId}:${version}"
  }

  /** Produce a new version of this archive with property `source=false`. */
  def noSource: Archive = copy(source = false)

  def noProvision: Archive = copy(provision = false)

  val pathPrefix: String = (System.getProperty("os.name")) match {
    case ("Windows" | "Windows 10") => "c:"
    case _ => ""
  }

  /** The file name of the archive file (extracted from the given URL). */
  val name = os.Path(pathPrefix + new java.net.URL(url).toURI().getPath()).last

  /** Generate a unique name for each eclipse archive to be used in the cross project. */
  val archiveId = s"${name}_${url.hashCode()}"
}

case class ProvisionRequest(archive: Archive, artifactId: String, version: String)
object ProvisionRequest {
  implicit def jsonify: upickle.default.ReadWriter[ProvisionRequest] = upickle.default.macroRW
}

object Archive {
  implicit def jsonify: upickle.default.ReadWriter[Archive] = upickle.default.macroRW

  /** Updated by [[Archive.apply]] */
  private var _bundles = Set[ProvisionRequest]()

  /**
   * Access to all auto-provisionable bundles.
   * Used by thirdparty project to download, extract and install the bundles as Maven artifacts.
   */
  def bundles: Set[ProvisionRequest] = _bundles
}
