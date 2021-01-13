package clicker.server

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import clicker._
import clicker.{BuyEquipment, GameState, SaveGames, UpdateGames}
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}

/** *
  * @param database      Reference to the database actor
  * @param configuration Custom configuration of the game (Used in Bonus Objective. Pass empty string before bonus)
  */
class ClickerServer(val database: ActorRef, configuration: String) extends Actor {
  val system = ActorSystem("Mine")
  //val actor = system.actorOf(Props(classOf[MyActor]))
  var SocketToActor: Map[SocketIOClient, ActorRef] = Map()
  var ActorToSocket: Map[ActorRef, SocketIOClient] = Map()

  val config: Configuration = new Configuration {
    setHostname("localhost")
    setPort(8080)
  }
  val MyServer: SocketIOServer = new SocketIOServer(config)
  MyServer.addDisconnectListener(new DisconnectionListener())
  MyServer.addEventListener("register", classOf[String], new Register(this))
  MyServer.addEventListener("clickGold", classOf[Nothing], new GoldClicked(this))
  MyServer.addEventListener("buy", classOf[String], new Buy(this))
  MyServer.start()
  override def receive: Receive = {
    case SaveGames => for((i,n) <- SocketToActor){n ! Save}
    case UpdateGames => for((i,n)<- SocketToActor){n ! Update}
    case GameState(gameState: String) => val S:SocketIOClient = ActorToSocket(sender()); S.sendEvent("gameState", gameState)
  }
  class DisconnectionListener() extends DisconnectListener {
    override def onDisconnect(socket: SocketIOClient): Unit = {
      SocketToActor(socket) ! PoisonPill
      ActorToSocket -= SocketToActor(socket)
      SocketToActor -= socket


    }
  }
  class Register(server: ClickerServer) extends DataListener[String] {
    override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
      val ACT = system.actorOf(Props(classOf[GameActor], username, server.database))
      server.SocketToActor += (socket -> ACT)
      server.ActorToSocket += (ACT -> socket)
    }
  }
  class GoldClicked(server: ClickerServer) extends DataListener[Nothing] {
    override def onData(client: SocketIOClient, n: Nothing, ackSender: AckRequest): Unit = {
      if(server.SocketToActor.contains(client)){
        val A = server.SocketToActor(client)
        A ! ClickGold
      }
    }
  }
  class Buy(server: ClickerServer) extends DataListener[String] {
    override def onData(socket: SocketIOClient, eqID: String, ackRequest: AckRequest): Unit = {
      if(server.SocketToActor.contains(socket)){
        val B = server.SocketToActor(socket)
        B ! BuyEquipment(eqID)
      }
    }
  }

  // Comment in server.stop() to stop your web socket server when the actor system shuts down. This will free
  // the port and allow to to test again immediately. Note that this doesn't work if you stop your server through
  // IntelliJ. If you use IntelliJ's stop button you will have to wait for the port to be freed before restarting
  // your server. By using the TestServer test suite and this method to stop the server you can avoid having to
  // wait before restarting while testing
  override def postStop(): Unit = {
    println("stopping server")
    //    server.stop()
  }
}


object ClickerServer {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem()

    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "test"))
    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, ""))

    actorSystem.scheduler.schedule(0.milliseconds, 100.milliseconds, server, UpdateGames)
    actorSystem.scheduler.schedule(0.milliseconds, 1000.milliseconds, server, SaveGames)
  }

}
