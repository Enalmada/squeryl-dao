package models

import dao.AuditUser
import org.squeryl.dsl.ast.OrderByArg
import dao.SquerylEntrypointForMyApp._
import dao._

case class User(id: Long = Entity.UnpersistedId, username: String = "") extends AuditUser

object User extends Dao[User](SampleSchema.userTable) {

  def buildOrderBy(row: User, sortBy: Dao.SortBy): OrderByArg = {
    if (sortBy.field.isEmpty) {
      row.id.asc
    } else {
      Dao.doSortBy(sortBy, sortBy.field match {
        case "id" => row.id.e
        case "username" => row.username.e
      })
    }
  }

  def list(queryOpt: Option[String], sortBy: Dao.SortBy, pageFilter: PageFilter): Page[User] = inTransaction {

    val queryWildcard = queryOpt.map("%" + _.toLowerCase + "%")

    val query = from(this.table)((user) =>
      where(lower(user.username) like queryWildcard.? or (lower(user.username) like queryWildcard.?)).
        select(user).
        orderBy(buildOrderBy(user, sortBy))
    ).distinct

    Dao.pageQuery(query, pageFilter)

  }

  /**
    * Instead of having many similar functions (getByName, getByEmail), consider just one
    * with options for each column.
    *
    * @param username
    * @return
    */
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