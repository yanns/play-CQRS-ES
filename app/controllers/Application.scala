package controllers

import play.api.mvc._

object Application extends Controller {

  def index = apiExplorer("/api")

  private def apiExplorer(filter: String) = Action {
    val pattern = raw"(\$$)(\w+)(<\[\^/\]\+>)"
    val doc = play.api.Play.current.routes.map {
      _.documentation
        .filter(_._2.startsWith(filter))
        .map { case (httpVerb, uri, controller) => addQueryParameters(httpVerb, uri, controller) }
        .map(d => d._1 -> d._2.replaceAll(pattern, "{$2}"))
    }

    Ok(views.html.apiexplorer(doc.getOrElse(Nil)))
  }

  def addQueryParameters(httpVerb: String, uri: String, controller: String) = {
    val begin = controller.lastIndexOf('(')
    if (begin == -1) {
      (httpVerb, uri, controller)
    } else {
      // extract all parameters from controller
      val cArgs = controller.substring(begin + 1, controller.lastIndexOf(')'))
      val args = cArgs.split(',').map(_.split(':')(0).trim)

      // keep only parameters that are not already mapped in the URL
      val nonMappedPathArgs = args
        .filterNot(_.isEmpty)
        .filterNot(a => uri.contains(s"$$$a<[^/]+>"))

      if (nonMappedPathArgs.isEmpty)
        (httpVerb, uri, controller)
      else {
        // show the non mapped parameters als query parameters
        (httpVerb, uri + nonMappedPathArgs.map(a => s"$a={$a}").mkString("?", "&", ""), controller)
      }
    }
  }


}