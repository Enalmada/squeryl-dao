package dao

import play.api.libs.json._

/**
  * Helpers for managing Select options for play forms and JSON.
  */
object SelectOptions {

  // SelectOption is (value, label)
  type SelectOption = (String, String)

  val AllSelectOption: Option[SelectOption] = Some(("", "All"))
  val UnselectedSelectOption: Option[SelectOption] = Some(("", "-- Select --"))
  val NoneSelectOption: Option[SelectOption] = Some(("", "None"))
  val EmptySelectOptions = List[SelectOption]()

  /**
    * Formats a SelectOption to JSON. Conforms with the X-Editable JQuery plugin format.
    */
  implicit object SelectOptionWrites extends Writes[SelectOption] {
    def writes(selectOption: SelectOption) = {
      Json.obj(
        "value" -> selectOption._1,
        "text" -> selectOption._2
      )
    }
  }

}