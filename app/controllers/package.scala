import play.api.libs.json.Json

package object controllers {

  def error(msg: String) = Json.obj("error" -> msg)
}
