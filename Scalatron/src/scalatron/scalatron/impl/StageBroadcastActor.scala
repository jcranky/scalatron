package scalatron.scalatron.impl

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.collection.mutable.MutableList

/**
  * Created by akaigorodov on 20/12/16.
  */

class StageBroadcastActor extends Actor {

  println("Actor initialized")

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))

  val handlers = MutableList[ActorRef]()

  def receive = {

    case UpdateStage(stage) =>
      handlers foreach {handler =>
        handler ! UpdateStage(stage)
      }

    case b @ Bound(localAddress) =>
      // do some logging or setup ...

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender()
      val handler = context.actorOf(Props(classOf[SimplisticHandler], connection))
      connection ! Register(handler)
      handlers += handler
  }

}

case class UpdateStage(stage: String)

class SimplisticHandler(connection: ActorRef) extends Actor {
  import Tcp._
  def receive = {
    case Received(data) => connection ! Write(data)
    case PeerClosed     => context stop self
    case UpdateStage(stage) => connection ! Write(ByteString(stage))
  }
}