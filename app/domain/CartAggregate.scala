package domain

import akka.actor.Props
import akka.event.LoggingReceive
import akka.persistence.SnapshotMetadata

object CartAggregate {

  import AggregateRoot._

  case class Cart(id: String, price: Double) extends State

  case class Initialize(price: Double) extends Command

  case class CartInitialized(price: Double) extends Event
  case object CartRemoved extends Event

  def props(id: String): Props = Props(new CartAggregate(id))
}

class CartAggregate(id: String) extends AggregateRoot {

  import AggregateRoot._
  import CartAggregate._

  override def persistenceId: String = id

  override def updateState(evt: Event): Unit = evt match {
    case CartInitialized(price) =>
      context.become(created)
      state = Cart(id, price)
    case CartRemoved =>
      context.become(removed)
      state = Removed
  }

  val initial = LoggingReceive {
    case Initialize(price) =>
      persist(CartInitialized(price))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  val created = LoggingReceive {
    case Remove =>
      persist(CartRemoved)(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  val removed = LoggingReceive {
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  override val receiveCommand: Receive = initial

  override def restoreFromSnapshot(metadata: SnapshotMetadata, state: State): Unit = {
    this.state = state
    state match {
      case Uninitialized => context become initial
      case Removed => context become removed
      case _: Cart => context become created
    }
  }

}
