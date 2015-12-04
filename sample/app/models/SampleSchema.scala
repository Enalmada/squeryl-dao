package models

import org.squeryl.Schema
import dao.SquerylEntrypointForMyApp._

object SampleSchema extends Schema {

  // override some defaults
  override def defaultLengthOfString = 255

  // automatically "snakify" property names to make column names (ex. UserName to user_name)
  override def columnNameFromPropertyName(property: String) = NamingConventionTransforms.snakify(property)

  // automatically "snakify" class names to make table names
  override def tableNameFromClassName(tableName: String): String = NamingConventionTransforms.snakify(tableName)

  // user is reserved word in postgres
  val userTable = table[User]("users")
  on(userTable)(u => declare(u.username is(unique, indexed("idx_user_username"))))

}


