package acmebuild.eclipse

object Archives {
  val thirdpartyEclipse = "de.acme.thirdparty.eclipse"

  @deprecated
  val sdk382win64 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/eclipse-SDK-3.8.2-win32-x86_64.zip",
    bundlePath = "eclipse/plugins/",
    groupId = thirdpartyEclipse
  )
  // @deprecated, but used for compat
  val rcp382 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/org.eclipse.rcp.source-3.8.2.zip",
    groupId = thirdpartyEclipse
  )
  @deprecated
  val rcp382_noSource = rcp382.noSource
  @deprecated
  val pde382 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/org.eclipse.pde.source-3.8.2.zip",
    groupId = thirdpartyEclipse
  )
  @deprecated
  val jdt382 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/org.eclipse.jdt.source-3.8.2.zip",
    groupId = thirdpartyEclipse
  )
  @deprecated
  val delta382 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/eclipse-3.8.2-delta-pack.zip",
    bundlePath = "eclipse/plugins/",
    groupId = thirdpartyEclipse
  )
  // Those bundles have no sources, so use from other archives if possible
  @deprecated
  val platform382 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops/R-3.8.2-201301310800/org.eclipse.platform-3.8.2.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
  val platform452 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.5.2-201602121500/org.eclipse.platform-4.5.2.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
  val rcp432 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.3.2-201402211700/org.eclipse.rcp.source-4.3.2.zip",
    groupId = thirdpartyEclipse
  )
  val rcp441 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.4.1-201409250400/org.eclipse.rcp.source-4.4.1.zip",
    groupId = thirdpartyEclipse
  )
  val rcp452 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.5.2-201602121500/org.eclipse.rcp.source-4.5.2.zip",
    groupId = thirdpartyEclipse
  )
  val rcp473 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.7.3-201803010715/org.eclipse.rcp.source-4.7.3.zip",
    groupId = thirdpartyEclipse
  )
  val rcp4_16 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.16-202006040540/org.eclipse.rcp.source-4.16.zip",
    groupId = thirdpartyEclipse
  )
  val platform4_16 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.16-202006040540/org.eclipse.platform-4.16.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
  val pde4_16 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.16-202006040540/org.eclipse.pde.source-4.16.zip",
    groupId = thirdpartyEclipse
  )
  val jdt4_16 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.16-202006040540/org.eclipse.jdt-4.16.zip",
    groupId = thirdpartyEclipse
  )
  // kein Java 8
  val rcp4_17 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.17-202009021800/org.eclipse.rcp.source-4.17.zip",
    groupId = thirdpartyEclipse
  )
  // kein Java 8
  val rcp4_20 = Archive(
    url = "https://download.eclipse.org/eclipse/downloads/drops4/R-4.20-202106111600/org.eclipse.rcp.source-4.20.zip",
    groupId = thirdpartyEclipse
  )
  // kein Java 8
  val sdk432win64 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.32-202406010610/eclipse-SDK-4.32-win32-x86_64.zip",
    groupId = thirdpartyEclipse,
    bundlePath = "eclipse/plugins"
  )
  val platform4_32 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.32-202406010610/org.eclipse.platform-4.32.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
  val platform4_35 = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.35-202502280140/org.eclipse.platform-4.35.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
  val swtbot230Archive = Archive(
    url = "http://ftp.fau.de/eclipse/technology/swtbot/releases/2.3.0/repository.zip",
    groupId = thirdpartyEclipse
  )
  val swtbot260Archive = Archive(
    url = "http://ftp.fau.de/eclipse/technology/swtbot/releases/2.6.0/repository.zip",
    groupId = thirdpartyEclipse
  )
  val eclipseTestingArchive = Archive(
    url = "https://archive.eclipse.org/eclipse/downloads/drops4/R-4.5.2-201602121500/eclipse-test-framework-4.5.2.zip",
    groupId = thirdpartyEclipse,
    source = false
  )
}
