
package dao

abstract class SquerylEnum extends Serializable {
  thisenum =>

  private val vset = scala.collection.mutable.SortedSet.empty[Value]
  private val vmap: scala.collection.mutable.Map[String, Value] = new scala.collection.mutable.HashMap

  class Value(name: String) extends org.squeryl.customtypes.StringField(name) with Ordered[Value] with Serializable {

    vset + this
    vmap(name) = this

    private[SquerylEnum] val outerEnum = thisenum

    override def toString = name

    override def compare(that: Value): Int = value compare that.value

    override def equals(other: Any) = other match {
      case that: SquerylEnum#Value => (outerEnum eq that.outerEnum) && (value == that.value)
      case _ => false
    }

  }

  protected final def Value(name: String): Value = new Value(name)

  final def values: Iterable[Value] = vset // vmap.values

  final def withName(s: String): Value = values.find(_.toString == s).get

}