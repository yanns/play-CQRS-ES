package controllers

import play.api.mvc._

object ApiExplorer extends Controller {

  case class EndPoint(httpVerb: String, uri: String, controller: String, example: Option[String] = None)

  def index = apiExplorer("/api")

  private def apiExplorer(filter: String) = Action {

    val doc = play.api.Play.current.routes.map {
      _.documentation
        .filter(_._2.startsWith(filter))
        .map(d => EndPoint(httpVerb = d._1, uri = d._2, controller = d._3))
        .map(addQueryParameters)
        .map(replacePathParam)
        .map(addExample)
    }

    Ok(views.html.apiexplorer(doc.getOrElse(Nil)))
  }

  def addQueryParameters(endPoint: EndPoint): EndPoint = {
    val controller = endPoint.controller
    val begin = controller.lastIndexOf('(')
    if (begin == -1) {
      endPoint
    } else {
      // extract all parameters from controller
      val cArgs = controller.substring(begin + 1, controller.lastIndexOf(')'))
      val args = cArgs.split(',').map(_.split(':')(0).trim)

      // keep only parameters that are not already mapped in the URL
      val nonMappedPathArgs = args
        .filterNot(_.isEmpty)
        .filterNot(a => endPoint.uri.contains(s"$$$a<[^/]+>"))

      if (nonMappedPathArgs.isEmpty)
        endPoint
      else
        // show the non mapped parameters als query parameters
        endPoint.copy(uri = endPoint.uri + nonMappedPathArgs.map(a => s"$a={$a}").mkString("?", "&", ""))
    }
  }

  val pattern = raw"(\$$)(\w+)(<\[\^/\]\+>)"
  def replacePathParam(endPoint: EndPoint) = endPoint.copy(uri = endPoint.uri.replaceAll(pattern, "{$2}"))

  def addExample(endPoint: EndPoint): EndPoint = {
    val examples: PartialFunction[(String, String), String] = {
      case ("POST", "/api/carts") =>
        """{
          |  "price": 56.93
          |}""".stripMargin
    }

    if (examples.isDefinedAt(endPoint.httpVerb, endPoint.uri))
      endPoint.copy(example = Some(examples.apply(endPoint.httpVerb, endPoint.uri)))
    else
      endPoint
  }

}