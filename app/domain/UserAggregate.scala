package domain

import akka.actor.Props
import akka.event.LoggingReceive
import akka.persistence.SnapshotMetadata
import common.VersionConflict

object UserAggregate {

  import AggregateRoot._

  case class User(id: String, email: String, version: Long) extends State

  case class CreateUser(email: String) extends Command
  case class UpdateEmail(email: String, version: Long) extends Command
  case class DeleteUser(version: Long) extends Command

  case class UserCreated(email: String, version: Long) extends Event
  case class EmailUpdated(email: String, version: Long) extends Event
  case class UserDeleted(version: Long) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends AggregateRoot {

  import AggregateRoot._
  import UserAggregate._

  override def persistenceId: String = id

  override def updateState(evt: Event): Unit = evt match {
    case UserCreated(email, version) =>
      context.become(created)
      state = User(id, email, version)
    case EmailUpdated(email, version) =>
      state match {
        case c: User => state = c.copy(email = email, version = version + 1)
        case _ => // ignore
      }
    case UserDeleted(version) =>
      context.become(removed)
      state = Removed
  }

  val initial = LoggingReceive {
    case CreateUser(email) =>
      persist(UserCreated(email, 1))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  def expectedVersion = state.asInstanceOf[User].version

  def withVersion(version: Long)(f: => Unit) =
    if (version != expectedVersion) sender() ! VersionConflict(id, expectedVersion, version)
    else f

  val created = LoggingReceive {
    case DeleteUser(version) =>
      withVersion(version) {
        persist(UserDeleted(version))(afterEventPersisted)
      }
    case GetState =>
      respond()
    case UpdateEmail(email, version) =>
      withVersion(version) {
        persist(EmailUpdated(email, version))(afterEventPersisted)
      }
    case KillAggregate =>
      context.stop(self)
  }

  val removed = LoggingReceive {
    case GetState | Remove | DeleteUser(_) =>
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
      case _: User => context become created
    }
  }

}
