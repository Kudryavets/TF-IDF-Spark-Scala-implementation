package util

import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.json4s.{JValue, NoTypeHints}

object JsonUtils {
  implicit val formats = Serialization.formats(NoTypeHints)

  def toJson(obj: AnyRef) : String = {
    write(obj)
  }

  def parseJson[T](json: String)(implicit m: Manifest[T]): T = {
    val result: JValue = parse(json)
    result.extract[T]
  }
}

