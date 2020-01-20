package service

import dao.Entity
import dao.SquerylEntrypointForMyApp._
import javax.inject.{Inject, Singleton}
import models.{SampleSchema, User}
import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.{Session, SessionFactory}
import play.api.Logger
import play.api.db.DBApi

@Singleton
class SquerylSession @Inject()(implicit val dbApi: DBApi, env: play.api.Environment) {
  val logger: Logger = Logger(this.getClass)

  logger.info("Squeryl session initializing")

  // this will actually run the database migrations on startup
  //applicationEvolutions

  // if play is being run in test mode, configure Squeryl to run using the H2 dialect because we want our tests to
  // run using an in memory database
  if (env.mode == play.api.Mode.Test) {
    // Squeryl SessionFactory
    SessionFactory.concreteFactory = Some(() =>
      Session.create(dbApi.database("default").getConnection(), new H2Adapter))

    // since this is an in-memory database that gets wiped out every time the app is shutdown,
    // we need to recreate the schema
    inTransaction {
      SampleSchema.printDdl
      SampleSchema.create
    }
  } else {
    // Squeryl SessionFactory
    SessionFactory.concreteFactory = Some(() => {
      val session = Session.create(dbApi.database("default").getConnection(), new PostgreSqlAdapter)
      //if (Play.isDev(app)) session.setLogger(msg => println(msg)) // Uncomment to enable logging
      session
    })
  }

  InitialData.insertUsers()
}

/** Initial set of data to be imported into the sample application. */
object InitialData {
  def insertUsers() {
    if (User.count == 0L) {
      implicit val user: User = User(Entity.UnpersistedId, username = "SampleUser")
      Seq(
        user
      ).foreach(u => User.save(u, 1)) // Bootstrapping in the first user
    }
  }
}
