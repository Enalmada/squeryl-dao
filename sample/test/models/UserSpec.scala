package models

import play.api.test.PlaySpecification
import test.support.SampleTest


class UserSpec extends PlaySpecification with SampleTest {

  "User" should {

    "be able to find" in running(fakeSampleApp) {
      val entity = User.save(User(username = "sampleUsername"))
      entity.isPersisted must beTrue
      entity.username mustEqual "sampleUsername"
      val found = User.findBy(username = Some("sampleUsername"))
      found.nonEmpty must beTrue
      found.exists(_.username == entity.username) must beTrue
    }

  }


}