package acmebuild

/** Test marker for integration tests (slow, runs docker). */
trait IntegrationTest

trait SmokeTest extends IntegrationTest