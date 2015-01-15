package controllers

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import domain.AggregateRoot.{Removed, Uninitialized, State}
import domain.CartAggregate.Cart
import global.Global
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.CartAggregateManager
import service.CartAggregateManager._
import akka.pattern._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object Carts extends Controller {

  val cartAggregateManager = Global.system.actorOf(CartAggregateManager.props)
  implicit val timeout = Timeout(300, TimeUnit.MILLISECONDS)

  implicit val cartToJson = Json.writes[Cart]

  def list = TODO

  def add = Action.async(parse.json) { request =>
    val price = (request.body \ "price").asOpt[Double]
    price.fold(Future.successful(BadRequest("please set the 'price' field"))) { p =>
      cartAggregateManager ? AddCart(p) map { result =>
        result.asInstanceOf[State] match {
          case c: Cart => Created(Json.toJson(c))
        }
      }
    }
  }

  def get(id: String) = Action.async {
    cartAggregateManager ? GetCart(id) map { result =>
      result.asInstanceOf[State] match {
        case c: Cart => Ok(Json.toJson(c))
        case Uninitialized | Removed => NotFound(Json.obj("error" -> s"cart '$id' not found"))
      }
    }
  }

}
