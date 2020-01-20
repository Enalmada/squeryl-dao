package support

import models.User
import play.api.Mode
import play.api.db.{Database, Databases}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers
import play.api.test.Helpers._
import service.SquerylSession

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}
/**
  * Base test trait
  */
trait SampleTest {

  // For unit tests that need cache injected
  // https://www.codatlas.com/github.com/playframework/playframework/HEAD/documentation/manual/working/scalaGuide/main/cache/code/ScalaCache.scala?line=40
  import play.api.cache.SyncCacheApi

  def withCache[T](block: SyncCacheApi => T) = {
    val app = fakeSampleApp
    running(app)(block(app.injector.instanceOf[SyncCacheApi]))
  }

  def withSqueryl[T](block: SquerylSession => T) = {
    val app = fakeSampleApp
    running(app)(block(app.injector.instanceOf[SquerylSession]))
  }

  // For tests that persist models that extend auditing columns
  implicit val auditUser = User(id = 1, username = "auditingUser")


  def withMyDatabase[T](block: Database => T) = {
    Databases.withInMemory(
      name = "mydatabase",
      urlOptions = Map(
        "MODE" -> "PostgreSQL"
      )
    )(block)
  }



  // Change this mode if you want to run MySQL instead.
  // Evolutions are handled in Global.scala at application start.
  //     .configuration(additionalConfiguration = inMemoryDatabase(options = Map("MODE" -> "PostgreSQL"))
  def fakeSampleApp = new GuiceApplicationBuilder()
    .configure("play.evolutions.enabled" -> "false")
    .configure("db.default.driver" -> "org.h2.Driver")
    .configure("db.default.url" -> "jdbc:h2:mem:play")
    .configure(Helpers.inMemoryDatabase(options = Map("MODE" -> "PostgreSQL")))
    .bindings(bind[SquerylSession].toSelf.eagerly())
    .in(Mode.Test)
    .build()

  // If you need to wait for something in a test
  def awaitUtil[T](awaitable: Awaitable[T], awaitTime: Duration = Duration(30, "seconds")): T = Await.result(awaitable, awaitTime)

}
