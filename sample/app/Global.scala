
import dao.Entity
import models.{User, SampleSchema}
import org.squeryl.adapters.{PostgreSqlAdapter, H2Adapter, MySQLAdapter}
import org.squeryl.{Session, SessionFactory}
import play.api.Play.current
import play.api.db.DB
import play.api.{Application, GlobalSettings, Logger, Play}
import dao.SquerylEntrypointForMyApp._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application started!")

    // if play is being run in test mode, configure Squeryl to run using the H2 dialect because we want our tests to
    // run using an in memory database
    if (Play.isTest(app)) {
      // Squeryl SessionFactory
      SessionFactory.concreteFactory = Some(() =>
        Session.create(DB.getConnection()(app), new H2Adapter))

      // since this is an in-memory database that gets wiped out every time the app is shutdown,
      // we need to recreate the schema
      inTransaction {
        SampleSchema.create
      }
    } else {
      // Squeryl SessionFactory
      SessionFactory.concreteFactory = Some(() => {
        val session = Session.create(DB.getConnection()(app), new PostgreSqlAdapter)
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
        ).foreach(User.save)
      }
    }
  }

}