package dao

import java.sql.Timestamp

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.dsl.ast.{OrderByArg, ExpressionNode, FunctionNode}
import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import dao.SelectOptions._

import scala.concurrent.duration._
import scala.reflect.ClassTag


object SquerylEntrypointForMyApp extends PrimitiveTypeMode

abstract class Dao[T <: IdEntity](val table: Table[T]) {

  import dao.SquerylEntrypointForMyApp._

  implicit object keyedEntityImplicit extends KeyedEntityDef[T, Long] {

    def getId(a: T) = a.id

    def isPersisted(a: T) = a.id > 0

    def idPropertyName = "id"

  }


  def cacheExpiration: Duration = 10 minutes

  def findAll()(implicit ct: ClassTag[T]): List[T] = inTransaction {
    Logger.debug(s"$ct findAll")
    table.allRows.toList
  }

  /**
    * Returns entity with the given id. as an option
    *
    * @param id
    * @return
    */
  def findById(id: Long)(implicit ct: ClassTag[T]): Option[T] = inTransaction {
    Logger.debug(s"$ct findById $id")
    table.lookup(id)
  }


  /**
    * Returns the entity by ID, first trying to use the cache, then falling back to the database only if the cache is
    * empty. Because our cache is not distributed, there is no guarantee that the cache will return the most recent
    * object, but that can be somewhat controlled by overriding {{{cacheExpiration}}}
    *
    * @param id the ID of the entity
    * @return the entity
    */
  def findByIdFromCache(id: Long)(implicit ct: ClassTag[T]): Option[T] = {
    Logger.debug(s"$ct findByIdFromCache $id")

    val expiration = cacheExpiration.toSeconds.toInt
    Cache.getOrElse[Option[T]](s"${ct}_option_$id", expiration) {
      findById(id)
    }
  }


  /**
    * Refreshes this entity so that optimistic locking version is updated
    *
    * @param entity
    * @return
    */
  def refresh(entity: T): T = inTransaction {
    table.lookup(entity.id).get
  }


  /**
    * Returns the entity by ID. This method does not return the entity as an option. Since you are doing a primary key
    * lookup, it should be safe to assume that the entity is there.
    *
    * @param id the ID of the entity
    * @return the entity
    */
  def get(id: Long)(implicit ct: ClassTag[T]): T = inTransaction {
    Logger.debug(s"$ct get $id")
    table.get(id)
  }


  /**
    * Returns all of the elements
    *
    * @param ids
    * @return
    */
  def get(ids: Seq[Long])(implicit ct: ClassTag[T]) = inTransaction {
    Logger.debug(s"$ct get ids:${ids.mkString(",")}")
    if (ids.nonEmpty) table.where(e => e.id in ids).toList.sortBy(id => ids.indexOf(id)) else List()
  }

  def count = inTransaction {
    countQuery.head.measures
  }

  def countQuery: Query[Measures[Long]] =
    from(table)(e => compute(countDistinct(1)))


  /**
    * Cache key used for gets. The cache key is a combination of the name of the implementation_type and the ID
    *
    * @param id the ID
    * @return the cacheKey
    */
  protected def cacheKey(id: Long)(implicit ct: ClassTag[T]) = s"${ct}_$id"


  /**
    * Returns the entity by ID, first trying to use the cache, then falling back to the database only if the cache is
    * empty. Because our cache is not distributed, there is no guarantee that the cache will return the most recent
    * object, but that can be somewhat controlled by overriding {{{cacheExpiration}}}
    *
    * @param id the ID of the entity
    * @return the entity
    */
  def getFromCache(id: Long)(implicit ct: ClassTag[T]): T = {

    Logger.debug(s"getFromCache ${ct.toString()} $id")

    val expiration = cacheExpiration.toSeconds.toInt
    Cache.getOrElse[T](cacheKey(id), expiration) {
      Logger.debug(s"getFromCache miss ${ct.toString()} $id")
      get(id)
    }
  }

  def findFromCache(id: Long)(implicit ct: ClassTag[T]): Option[T] = {
    Logger.debug(s"findFromCache ${ct.toString()} $id")

    val expiration = cacheExpiration.toSeconds.toInt
    Cache.getOrElse[Option[T]](cacheKey(id), expiration) {
      Logger.debug(s"findFromCache miss ${ct.toString()} $id")
      findById(id)
    }
  }


  /**
    * Returns a list of entities, maintaining the order of the IDs specified.
    *
    * This method tries to pull each entity out of the cache if it can. If it can't, it will query the database for only
    * the missing entities, then put them in the cache for next time
    *
    * @param ids the IDs
    * @return the entities
    */
  def getFromCache(ids: Seq[Long])(implicit ct: ClassTag[T]): List[T] = {
    Logger.debug(s"$ct getFromCache ${ids.mkString(",")}")

    val cacheResults = for (id <- ids.toList) yield Cache.getAs[T](cacheKey(id))

    //separate out the results that were in the cache from the ones that were not in the cache
    val alreadyCachedResults = cacheResults collect { case Some(x: T) => x }
    val alreadyCachedIds = alreadyCachedResults.map(_.id)
    val idsOfUncachedResults = ids.filterNot(alreadyCachedIds.contains(_))

    val resultsFromDB = if (idsOfUncachedResults.nonEmpty) get(idsOfUncachedResults) else List()

    // put the results fetched by the DB into the cache for next time
    for (entity <- resultsFromDB) Cache.set(cacheKey(entity.id), entity, cacheExpiration.toSeconds.toInt)

    // combine the results and resort
    (alreadyCachedResults ++ resultsFromDB).sortBy(entity => ids.indexOf(entity.id))
  }


  /**
    * Returns entities with the specified ids.
    *
    * @param ids
    * @return
    */
  def findByIds(ids: Seq[Long])(implicit ct: ClassTag[T]): Seq[T] = inTransaction {

    Logger.debug(s"$ct findByIds ${ids.mkString(",")}")

    // Get entities. There is no guarantee order will be the same as ids.
    val unsortedEntities = table.where(t => t.id in ids).toSeq

    // Create lookup map
    val entityMap = unsortedEntities.map(t => (t.id, t)).toMap

    // No guarantee all the ids will come back
    val unsortedEntityIds: Seq[Long] = unsortedEntities.map(entity => entity.id)

    // Take original order of ids and build Seq of entities in same order
    ids.filter(id => unsortedEntityIds.contains(id)).map(id => entityMap(id))
  }

  /**
    * Returns true if the id corresponds to an existing entity.
    *
    * @param id
    * @return
    */
  def isDefined(id: Long)(implicit ct: ClassTag[T]): Boolean = inTransaction {
    Logger.debug(s"$ct isDefined $id")
    findById(id).isDefined
  }

  /**
    * Handles pagination of a query. Returns a Page object with results and page properties.
    *
    * @param query
    * @param pageFilter
    * @return
    */
  def pageQuery(query: Query[T], pageFilter: PageFilter): Page[T] = inTransaction(Dao.pageQuery(query, pageFilter))

  /**
    * Wraps saving of an entity. This is done to populate auditing fields.
    *
    * @param entity the entity to save
    * @param user   the user who is responsible
    */
  def save(entity: T)(implicit user: AuditUser, ct: ClassTag[T]): T = save(entity, user.id)

  /**
    * Wraps saving of an entity. This is done to populate auditing fields.
    *
    * @param entity      the entity to save
    * @param auditUserId the user who is responsible
    */
  def save(entity: T, auditUserId: Long)(implicit ct: ClassTag[T]): T = inTransaction {

    // Set auditing properties
    entity match {
      case auditedEntity: AuditedEntity =>

        // Set "Entry" fields
        if (auditedEntity.isPersisted) {

          // If this is an edit, copy "Entry" fields from previous version as otherwise they will be overwritten by evolutions values.
          // This is a temporary hack until we have a better solution for auditing.
          val previousVersion = findById(entity.id)
          previousVersion match {
            case Some(x: AuditedEntity) => auditedEntity.createdById = x.createdById; auditedEntity.dateCreated = x.dateCreated
            case None =>
            case _ =>
          }
        } else {
          // New entity, set initial values
          auditedEntity.dateCreated = new Timestamp(System.currentTimeMillis())
          auditedEntity.createdById = auditUserId
        }

        // Set "Last Updated" properties
        auditedEntity.lastUpdate = new Timestamp(System.currentTimeMillis)
        auditedEntity.updatedById = auditUserId

      case _ =>
    }

    // Refresh is a workaround to make sure the occVersionId is returned correctly
    refresh(table.insertOrUpdate(entity))
  }

  def delete(id: Long): Boolean = inTransaction {
    val deletedRows = this.table.deleteWhere(_.id === id)
    deletedRows > 0
  }

  //def options = selectOptions(Some((Entity.UnpersistedId.toString, "-- Select --")), findAll)

  def selectOptions(firstSelectOption: Option[SelectOption], elements: => List[T])(labelMapper: T => String): List[SelectOption] = {

    val baseSelectOptions = elements.map(x => (x.id.toString, labelMapper(x)))

    firstSelectOption match {

      case Some(selectOption) => selectOption :: baseSelectOptions
      case None => baseSelectOptions
    }
  }


  /**
    * Updates the many-to-many associations between an antity that this Dao is for, and its associations
    *
    * @param entity       the the entity that you want to update the associations for
    * @param associations the complete list of objects that you want to be associated with this entity
    * @return the refreshed entity
    */
  protected def updateManyToMany[E <: IdEntity, L <: KeyedEntity[_]](entity: T, associations: Seq[E], query: Query[E] with ManyToMany[E, L])(implicit user: AuditUser, ct: ClassTag[T]): T = inTransaction {

    // First dissociate any existing entities that aren't in the updated list
    val existingRelationships = query.toList

    existingRelationships.foreach(existing => {
      if (!associations.contains(existing)) {
        query.dissociate(existing)
      }
    })

    // Add/update associations contained in the updated list
    associations.foreach(newRelationship => {
      if (!existingRelationships.contains(newRelationship)) {
        // Add new association
        query.associate(newRelationship)
      }
    })

    // save to update auditing
    save(entity)
  }


}


trait CustomSQLFunctions {

  import dao.SquerylEntrypointForMyApp._

  class SHA1(e: TypedExpression[String, TString])
    extends FunctionNode("sha1", Seq(e)) with TypedExpression[String, TString] {
    lazy val mapper = stringTEF.createOutMapper
  }

  class SIN(e: TypedExpression[Double, TDouble])
    extends FunctionNode("sin", Seq(e)) with TypedExpression[Double, TDouble] {
    lazy val mapper = doubleTEF.createOutMapper
  }

  class COS(e: TypedExpression[Double, TDouble])
    extends FunctionNode("cos", Seq(e)) with TypedExpression[Double, TDouble] {
    lazy val mapper = doubleTEF.createOutMapper
  }

  class ACOS(e: TypedExpression[Double, TDouble])
    extends FunctionNode("acos", Seq(e)) with TypedExpression[Double, TDouble] {
    lazy val mapper = doubleTEF.createOutMapper
  }

  def sha1(e: TypedExpression[String, TString]) = new SHA1(e)

  def sin(e: TypedExpression[Double, TDouble]) = new SIN(e)

  def cos(e: TypedExpression[Double, TDouble]) = new COS(e)

  def acos(e: TypedExpression[Double, TDouble]) = new ACOS(e)
}

// end Dao


object Dao {

  import dao.SquerylEntrypointForMyApp._


  sealed trait SortOrder

  case object Asc extends SortOrder

  case object Desc extends SortOrder

  trait SortByField

  case class SortBy(field: String, order: SortOrder = Asc)

  def doSortBy(sortBy: Dao.SortBy, nameMatch: ExpressionNode): OrderByArg =
    sortBy.order match {
      case Dao.Asc => new OrderByArg(nameMatch).asc
      case Dao.Desc => new OrderByArg(nameMatch).desc
    }

  /**
    * Handles pagination of a query. Returns a Page object with results and page properties.
    *
    * @param query      the [[org.squeryl.Query]]
    * @param pageFilter the [[dao.PageFilter]]
    * @return the [[dao.Page]]
    */
  def pageQuery[T](query: Query[T], pageFilter: PageFilter): Page[T] = inTransaction {

    // Using a hack to determine if there is a next page by returning 1 more result than page size.
    // If the query returns pageSize + 1 results, there's a next page.
    val items = query.page(pageFilter.offset, pageFilter.pageSize + 1).toSeq
    if (items.size > pageFilter.pageSize) {
      Page(items.dropRight(1), pageFilter, hasPrev = pageFilter.page != 0, hasNext = true)
    } else {
      Page(items, pageFilter, hasPrev = pageFilter.page != 0, hasNext = false)
    }
  }
}


/**
  * Pagination support.
  */
case class PageFilter(page: Int = 0, pageSize: Int = 50) {
  def offset = page * pageSize
}

case class Page[+T](items: Seq[T], pageFilter: PageFilter, hasPrev: Boolean, hasNext: Boolean) {
  lazy val prev = Option(pageFilter.page - 1).filter(_ >= 0)
  lazy val current = pageFilter.page
  lazy val next = Option(pageFilter.page + 1).filter(_ => hasNext)
  lazy val from = pageFilter.offset + 1
  lazy val to = pageFilter.offset + items.size
  lazy val total = 0
}