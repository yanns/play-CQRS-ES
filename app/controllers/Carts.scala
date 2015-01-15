package controllers

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import domain.AggregateRoot.{Removed, State, Uninitialized}
import domain.CartAggregate.Cart
import global.Global
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.AggregateManager.Command
import service.CartAggregateManager
import service.CartAggregateManager._

import scala.concurrent.Future

object Carts extends Controller {

  implicit val cartToJson = Json.writes[Cart]

  def list = TODO


  def add = Action.async(parse.json) { request =>
    val price = (request.body \ "price").asOpt[Double]

    def noPrice = Future.successful(BadRequest(error("please set the 'price' field")))

    price.fold(noPrice) { p =>
      sendCmd(AddCart(p)) map {
        case c: Cart => Created(Json.toJson(c))
      }
    }
  }


  def get(id: String) = Action.async {
    sendCmd(GetCart(id)) map {
      case c: Cart => Ok(Json.toJson(c))
      case Uninitialized | Removed => NotFound(error(s"cart '$id' not found"))
    }
  }


  def delete(id: String) = Action.async {
    sendCmd(DeleteCart(id)) map {
      case _ => NoContent
    }
  }


  val cartAggregateManager = Global.system.actorOf(CartAggregateManager.props)

  def sendCmd(cmd: Command): Future[State] = {
    import akka.pattern.ask
    import scala.concurrent.duration._

    implicit val timeout = Timeout(3.seconds)

    val result = ask(cartAggregateManager, cmd)
    result map (_.asInstanceOf[State])
  }

}
