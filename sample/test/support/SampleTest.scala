package test.support

import models.User
import play.api.http.Writeable
import play.api.mvc.AnyContentAsMultipartFormData
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

/**
 * Base test. Contains an implicit audit user and convenience methods for creating persisted objects.
 */
trait SampleTest {

  import play.api.cache.CacheApi

  // https://www.codatlas.com/github.com/playframework/playframework/HEAD/documentation/manual/working/scalaGuide/main/cache/code/ScalaCache.scala?line=40
  def withCache[T](block: CacheApi => T) = {
    val app = fakeSampleApp
    running(app)(block(app.injector.instanceOf[CacheApi]))
  }


  // This is to be able to post test forms
  // http://tech.fongmun.com/post/125479939452/test-multipartformdata-in-play
  // other option: https://stackoverflow.com/questions/19658766/play-framework-testing-using-multipartformdata-in-a-fakerequest
  // etc: https://github.com/knoldus/playing-multipartform
  implicit val anyContentAsMultipartFormWritable: Writeable[AnyContentAsMultipartFormData] = {
    MultipartFormDataWritable.singleton.map(_.mdf)
  }

  implicit val auditUser = User(id = 1, username = "auditingUser")

  def fakeSampleApp = FakeApplication(additionalConfiguration = inMemoryDatabase(options = Map("MODE" -> "PostgreSQL"))
    + ("play.evolutions.enabled" -> "false")
  )

  /**
   * Simple utility for dealing with Futures. This method waits for futures to complete, so you can test everything
   * sequentially.
   *
   * @param awaitable the code returning a future to wait for
   * @tparam T the result type
   * @return the result
   */
  def awaitUtil[T](awaitable: Awaitable[T], awaitTime: Duration = Duration(30, "seconds")): T = Await.result(awaitable, awaitTime)

}