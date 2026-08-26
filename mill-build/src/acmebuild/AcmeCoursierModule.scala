package acmebuild

import mill.javalib.CoursierModule
import mill.{T, Task}

/** Coursier settings for Acme. */
trait AcmeCoursierModule extends CoursierModule {
  override def repositoriesTask = Task.Anon {
    (super.repositoriesTask() ++ BuildSettings.acmeRepos).distinct
  }
}
