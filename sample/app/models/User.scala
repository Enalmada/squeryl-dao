package models

import dao.AuditUser
import play.api.cache.CacheApi
import dao.SquerylEntrypointForMyApp._
import dao._

case class User(id: Long = Entity.UnpersistedId, username: String = "") extends AuditUser

object User extends Dao[User](SampleSchema.userTable) {


  def list(queryOpt: Option[String], pageFilter: PageFilter)(implicit cache: CacheApi): Page[User] = inTransaction {

    val queryWildcard = queryOpt.map("%" + _.toLowerCase + "%")

    val query = from(this.table)((user) =>
      where(lower(user.username) like queryWildcard.? or (lower(user.username) like queryWildcard.?)).
        select(user).
        orderBy(user.username.asc)
    ).distinct

    Dao.pageQuery(query, pageFilter)

  }

  def findBy(username: Option[String] = None): List[User] = inTransaction {

    val usernameLower = username.map(_.toLowerCase)

    from(this.table)(e => where(
      lower(e.username) === usernameLower.?
    )
      select e).toList
  }

  def create(username: String)(implicit user: AuditUser): User = {
    val newUser = User.save(User(username = username))
    newUser
  }


}