package akka.persistence.mongo.journal

import akka.persistence.PersistentRepr

import scala.collection.immutable.Seq

class MyJournal extends CasbahJournal {
  override def writeMessages(persistentBatch: Seq[PersistentRepr]): Unit = {
    log.debug(persistentBatch.map(_.payload).mkString(","))
    super.writeMessages(persistentBatch)
  }
}
