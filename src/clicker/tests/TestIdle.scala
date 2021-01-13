package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{BuyEquipment, GameState}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import clicker._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class TestIdle extends TestKit(ActorSystem("TestIdle"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "earn the correct idle income" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      var i: Int = 0
      gameActor ! Update
      var gstate: GameState = expectMsgType[GameState](2000.millis)
      var gameStateJ: String = gstate.gameState
      var ParseIt: JsValue = Json.parse(gameStateJ)
      var Gold: Double = (ParseIt \ "gold").as[Double]
      val Uname: String = (ParseIt \ "username").as[String]

      while(i < 200){
        gameActor!ClickGold
        i+=1
      }
      expectNoMessage(500.millis)
      gameActor ! Update
      gstate = expectMsgType[GameState](1000.millis)
      gameStateJ = gstate.gameState
      ParseIt = Json.parse(gameStateJ)
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold == 200, "this one")
      assert(Uname == "username")
      i = 0
      gameActor!BuyEquipment("excavator")
      gameActor!BuyEquipment("excavator")
      expectNoMessage(1000.millis)
      gameActor!Update
      gstate = expectMsgType[GameState](400.millis)
      gameStateJ = gstate.gameState
      ParseIt = Json.parse(gameStateJ)
      val ECost = (ParseIt\"equipment"\"excavator"\"cost").as[Double]
      val ECount = (ParseIt\"equipment"\"excavator"\"numberOwned").as[Int]
      Gold = (ParseIt\ "gold").as[Double]
      assert(ECount == 1)
      assert(Gold > 9.8 && Gold < 10.3)
      while(i < 165){
        gameActor!ClickGold
        i+=1
      }
      gameActor ! BuyEquipment("mine")
      expectNoMessage(1000.millis)
      gameActor!Update
      gstate = expectMsgType[GameState](400.millis)
      gameStateJ = gstate.gameState
      ParseIt = Json.parse(gameStateJ)
      val MCost = (ParseIt\"equipment"\"mine"\"cost").as[Double]
      val MCount = (ParseIt\"equipment"\"mine"\"numberOwned").as[Int]
      Gold = (ParseIt\ "gold").as[Double]
      assert(MCount == 1)
      assert(Gold > 110 && Gold < 130)
    }
  }


}
