package controllers

import play.api.i18n.I18nSupport

// Abstract out common controller functions
trait BaseController extends I18nSupport {

  val defaultPageSize = 50

  def stringOpt(value: String) = Option(value).filter(_.trim.nonEmpty)

  def longOpt(value: String) = Option(value).filter(_.trim.nonEmpty).map(_.toLong)

  def intOpt(value: String) = Option(value).filter(_.trim.nonEmpty).map(_.toInt)

  //def forbidden(implicit flash: Flash) = Forbidden(views.html.forbidden())

  //def notFound(implicit flash: Flash) = NotFound(views.html.notFound())
}
