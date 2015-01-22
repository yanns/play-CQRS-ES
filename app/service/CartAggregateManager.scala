package service

import java.util.UUID

import akka.actor.Props
import akka.event.LoggingReceive
import domain.AggregateRoot.{Remove, GetState}
import domain.CartAggregate

object CartAggregateManager {

  import AggregateManager._

  case class AddCart(price: Double) extends Command
  case class GetCart(id: String) extends Command
  case class UpdatePriceCmd(id: String, price: Double) extends Command
  case class DeleteCart(id: String) extends Command

  def props: Props = Props(new CartAggregateManager)
}

class CartAggregateManager extends AggregateManager {

  import AggregateManager._
  import CartAggregateManager._
  import CartAggregate._

  def processCommand = LoggingReceive {
    case AddCart(price) =>
      val id = "cart-" + UUID.randomUUID().toString
      processAggregateCommand(id, Initialize(price))
    case GetCart(id) =>
      processAggregateCommand(id, GetState)
    case UpdatePriceCmd(id, price) =>
      processAggregateCommand(id, UpdatePrice(price))
    case DeleteCart(id) =>
      processAggregateCommand(id, Remove)
  }

  override def aggregateProps(id: String): Props = CartAggregate.props(id)
}
