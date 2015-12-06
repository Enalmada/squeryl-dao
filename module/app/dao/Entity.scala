package dao

import java.sql.Timestamp

import org.squeryl.{KeyedEntity, Optimistic}

trait IdEntity extends KeyedEntity[Long] {

  def id: Long

  override def isPersisted = id > 0
}

trait AuditedEntity extends IdEntity with Optimistic {

  var dateCreated: Timestamp = new Timestamp(System.currentTimeMillis)
  var createdById: Long = Entity.UnpersistedId
  var lastUpdate: Timestamp = new Timestamp(System.currentTimeMillis)
  var updatedById: Long = Entity.UnpersistedId

  // todo these should probably be cached
  //def createdBy: User = User.get(createdById)
  //def updatedBy: User = User.get(updatedById)

}

trait AuditUser extends AuditedEntity {
  def id: Long
}

object Entity {
  val UnpersistedId: Long = -1
}