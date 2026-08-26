package acmebuild

object BuildSettings {

  val isMac = System.getProperty("os.name") == "Mac OS X"

  val acmeRepos = Seq(
    // TODO register in nexus
    // coursier.maven.MavenRepository("https://dl.bintray.com/tabmo/maven"),
    coursier.maven.MavenRepository("https://www.myget.org/F/tabmo-public/maven/"),
    // Apache Pekko is only available as snapshot
//    coursier.maven.MavenRepository("https://repository.apache.org/content/groups/snapshots").withChanging(true)
  )
}
