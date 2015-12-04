package controllers

import javax.inject.Inject

import models.User
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import dao.PageFilter

class UserController @Inject()(implicit val messagesApi: MessagesApi, cache: CacheApi) extends BaseController {

  implicit val listPage = routes.UserController.list()

  // This is so we can show the category error on the category field.  Need access to id and parentId to compare
  val duplicateUsernameCheck = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      // "data" lets you access all form data values
      val id = data.getOrElse("id", "")
      Logger.debug(s"userID: $id")
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
      "username" -> of(duplicateUsernameCheck)
    ) { (id, username) => {
      Logger.debug(s"userID: $id")
      id.map(User.get).getOrElse(User()).copy(username = username)
    }
    } { user =>
      Some(Some(user.id), user.username)
    }
  )


  /**
    * Display the paginated list of Users.
    *
    * @param page  Current page number (starts from 0)
    * @param query Filter applied on User names
    */
  def list(page: Int, query: String) = Action { implicit request =>
    val form: Form[String] = Form("query" -> text).fill(query)
    val queryOpt = stringOpt(query)
    val pageFilter = PageFilter(page, 20)
    val list = User.list(queryOpt, pageFilter)
    Ok(views.html.listUser(list, form))
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
        Ok(views.html.editUserForm(id, form))
    }
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the User to edit
    */
  def update(id: Long) = Action { implicit request =>

    implicit val auditUser = User.findAll.head


    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editUserForm(id, formWithErrors)),
      success = userForm => {
        User.save(userForm)
        val flashMessage = "User has been updated"
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