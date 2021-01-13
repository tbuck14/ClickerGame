package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{BuyEquipment, GameState}
import clicker.database.DatabaseActor
import clicker._
import clicker.model.GameActor
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class TestSave extends TestKit(ActorSystem("TestSave"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "save and load properly" in {
      expectNoMessage(50.millis)
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "Trevor", database))
      var i: Int = 0
      while(i<2000) {
        gameActor ! ClickGold
        i += 1
      }
      expectNoMessage(50.millis)
      gameActor ! Update
      var gstate: GameState = expectMsgType[GameState](1000.millis)
      var gameStateJ: String = gstate.gameState
      var ParseIt: JsValue = Json.parse(gameStateJ)
      var Gold: Double = (ParseIt\ "gold").as[Double]
      assert(Gold == 2000, "first")
      gameActor ! BuyEquipment("shovel")
      gameActor ! BuyEquipment("mine")
      gameActor ! BuyEquipment("excavator")
      expectNoMessage(1000.millis)
      gameActor ! Update
      gstate = expectMsgType[GameState](1000.millis)
      ParseIt = Json.parse(gstate.gameState)
      var ECount = (ParseIt\"equipment"\"excavator"\"numberOwned").as[Int]
      var SCount = (ParseIt\"equipment"\"shovel"\"numberOwned").as[Int]
      var MCount = (ParseIt\"equipment"\"mine"\"numberOwned").as[Int]
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold > 800 && Gold < 1000)
      assert(ECount == 1)
      assert(SCount == 1)
      assert(MCount == 1)
      gameActor ! Save
      gameActor ! Update
      gstate = expectMsgType[GameState](1000.millis)
      gameActor ! GameState(gstate.gameState)
      gameActor ! Update
      ECount = (ParseIt\"equipment"\"excavator"\"numberOwned").as[Int]
      SCount = (ParseIt\"equipment"\"shovel"\"numberOwned").as[Int]
      MCount = (ParseIt\"equipment"\"mine"\"numberOwned").as[Int]
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold > 800 && Gold < 1200)
      assert(ECount == 1)
      assert(SCount == 1)
      assert(MCount == 1)



      gameActor ! Save
      Thread.sleep(1000)
      val Aboy = system.actorOf(Props(classOf[GameActor], "Trevor", database))
      Aboy ! Update
      gstate = expectMsgType[GameState](1000.millis)
      gameStateJ = gstate.gameState
      ParseIt = Json.parse(gameStateJ)
      ECount = (ParseIt\"equipment"\"excavator"\"numberOwned").as[Int]
      SCount = (ParseIt\"equipment"\"shovel"\"numberOwned").as[Int]
      MCount = (ParseIt\"equipment"\"mine"\"numberOwned").as[Int]
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold > 925 && Gold < 985)
      assert(ECount == 1)
      assert(SCount == 1)
      assert(MCount == 1)
    }
  }


}
