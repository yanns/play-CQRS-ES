package service

import java.util.UUID

import akka.actor.Props
import akka.event.LoggingReceive
import domain.AggregateRoot.GetState
import domain.UserAggregate

object UserAggregateManager {

  import service.AggregateManager._

  case class AddUserCmd(email: String) extends Command
  case class GetUserCmd(id: String) extends Command
  case class UpdateEmailCmd(id: String, email: String, version: Long) extends Command
  case class DeleteUserCmd(id: String, version: Long) extends Command

  def props: Props = Props(new UserAggregateManager)
}

class UserAggregateManager extends AggregateManager {

  import domain.UserAggregate._
  import service.UserAggregateManager._

  def processCommand = LoggingReceive {
    case AddUserCmd(email) =>
      val id = "user-" + UUID.randomUUID().toString
      processAggregateCommand(id, CreateUser(email))
    case GetUserCmd(id) =>
      processAggregateCommand(id, GetState)
    case UpdateEmailCmd(id, email, version) =>
      processAggregateCommand(id, UpdateEmail(email, version))
    case DeleteUserCmd(id, version) =>
      processAggregateCommand(id, DeleteUser(version))
  }

  override def aggregateProps(id: String): Props = UserAggregate.props(id)
}
