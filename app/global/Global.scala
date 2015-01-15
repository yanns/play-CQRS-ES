package global

import akka.actor.ActorSystem
import play.api.libs.json.Json
import play.api.mvc.{Result, RequestHeader}
import play.api.mvc.Results._
import play.api.{Application, GlobalSettings}

import scala.concurrent.Future

object Global extends GlobalSettings {

  var system: ActorSystem = _

  override def onStart(app: Application): Unit = {
    system = ActorSystem("es-actor-system")
  }

  override def onStop(app: Application): Unit = {
    system.shutdown()
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    Future.successful(BadRequest(Json.obj("error" -> error)))
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    Future.successful(InternalServerError(Json.obj("error" -> ex.getMessage)))
  }
}
