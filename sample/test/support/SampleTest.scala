package support

import models.User
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

/**
  * Base test trait
  */
trait SampleTest {

  // For unit tests that need cache injected
  // https://www.codatlas.com/github.com/playframework/playframework/HEAD/documentation/manual/working/scalaGuide/main/cache/code/ScalaCache.scala?line=40
  import play.api.cache.CacheApi

  def withCache[T](block: CacheApi => T) = {
    val app = fakeSampleApp
    running(app)(block(app.injector.instanceOf[CacheApi]))
  }

  // For tests that persist models that extend auditing columns
  implicit val auditUser = User(id = 1, username = "auditingUser")

  // Change this mode if you want to run MySQL instead.
  // Evolutions are handled in Global.scala at application start.
  def fakeSampleApp = FakeApplication(additionalConfiguration = inMemoryDatabase(options = Map("MODE" -> "PostgreSQL"))
    + ("play.evolutions.enabled" -> "false")
  )

  // If you need to wait for something in a test
  def awaitUtil[T](awaitable: Awaitable[T], awaitTime: Duration = Duration(30, "seconds")): T = Await.result(awaitable, awaitTime)

}