package common


case class Error(message: String)
case class VersionConflict(id: String, expectedVersion: Long, receivedVersion: Long)

case class Acknowledge(id: String)
