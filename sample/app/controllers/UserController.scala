package controllers

import dao.{Dao, PageFilter}
import javax.inject.Inject
import models.User
import play.api.cache.SyncCacheApi
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.mvc.{AbstractController, Call, ControllerComponents}
import service.SquerylSession

// An example of a model controller with common crud actions
class UserController @Inject()(implicit val components: ControllerComponents, messagesApi: MessagesApi, cache: SyncCacheApi, squerylSession: SquerylSession) extends AbstractController(components) with BaseController {

  implicit val listPage = routes.UserController.list()

  def GO_HOME()(implicit listPage: Call) = Redirect(listPage)

  // Manual bind so we can check for duplicate name and report custom error with link to offending object
  // since inline validation doesn't give you access to the other data and we need the id in addition to the name
  val duplicateUsernameCheck = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      // "data" lets you access all form data values
      val id = data.getOrElse("id", "")
      val username = data.getOrElse("username", "")
      val otherUser: Option[User] = User.findBy(username = Some(username)).headOption
      if (username != "" && otherUser.isDefined && id.toLong != otherUser.get.id) {
        Left(List(FormError("username", "Username is already taken by " + routes.UserController.edit(otherUser.get.id))))
      } else {
        Right(username)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }

  def userForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> of(duplicateUsernameCheck),
      "occVersionNumber" -> default(number, 0)
    ) { (id, username, occVersionNumber) => {
      id.map(User.get).getOrElse(User()).copy(username = username, occVersionNumber = occVersionNumber)
    }
    } { user =>
      Some(Some(user.id), user.username, user.occVersionNumber)
    }
      .verifying("This user has just been changed by someone else.",
        i => !User.changed(if (i.id < 0) None else Some(i.id), i.occVersionNumber))
  )


  /**
   * Display the paginated list of Users.
   *
   * @param page  Current page number (starts from 0)
   * @param query Filter applied on User names
   */
  def list(page: Int, sortBy: String, sortOrder: String, query: String) = Action { implicit request =>
    val form: Form[String] = Form("query" -> text).fill(query)
    val queryOpt = stringOpt(query)
    val pageFilter = PageFilter(page, 20)
    val dbSortOrder = if (sortOrder == "desc") Dao.Desc else Dao.Asc
    val list = User.list(queryOpt, Dao.SortBy(sortBy, dbSortOrder), pageFilter)
    Ok(views.html.user.listUser(list, form, sortBy, sortOrder))
  }

  def create() = Action { implicit request =>
    val form = userForm.fill(User())
    Ok(views.html.user.createUserForm(form))
  }


  /**
   * Display the 'edit form' of a existing User.
   *
   * @param id Id of the User to edit
   */
  def edit(id: Long) = Action { implicit request =>
    User.findById(id) match {
      case None => NotFound
      case Some(user) =>
        val form = userForm.fill(user)
        Ok(views.html.user.editUserForm(id, form))
    }
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the User to edit
   */
  def update(id: Long) = Action { implicit request =>

    implicit val auditUser = User.findAll.head // Replace with loggedIn user from your authentication framework

    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.editUserForm(id, formWithErrors)),
      success = userForm => {
        User.save(userForm)
        val flashMessage = "User has been updated"
        GO_HOME.flashing("success" -> flashMessage)
      })

  }

  def save = Action { implicit request =>

    implicit val auditUser = User.findAll.head // Replace with loggedIn user from your authentication framework

    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.createUserForm(formWithErrors)),
      success = userForm => {
        User.save(userForm)
        val flashMessage = "User has been created"
        GO_HOME.flashing("success" -> flashMessage)
      })
  }

  /**
   * Handle User deletion
   */
  def delete(id: Long) = Action { implicit request =>
    val user: User = User.get(id)
    User.delete(user.id)
    GO_HOME.flashing("success" -> "User has been deleted")
  }


}
