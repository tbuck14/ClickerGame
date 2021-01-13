package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class TestClicks extends TestKit(ActorSystem("TestClicks"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "react to user clicks with shovels appropriately" in {

      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "Trevor", database))

      gameActor ! ClickGold
      gameActor ! ClickGold

      // Wait for 50ms to ensure ClickGold messages resolve before moving on
      expectNoMessage(50.millis)

      // Send Update message and expect a GameState message in response
      // Wait up to 100ms for the response
      gameActor ! Update
      var gs: GameState = expectMsgType[GameState](600.millis)
      var gameStateJSON: String = gs.gameState
      var ParseIt: JsValue = Json.parse(gameStateJSON)
      var Gold: Double = (ParseIt \ "gold").as[Double]
      // Parse gameState and use assert to test each value
      // TODO
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! BuyEquipment("shovel")
      gameActor ! ClickGold
      expectNoMessage(50.millis)
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gameStateJSON = gs.gameState
      ParseIt = Json.parse(gameStateJSON)
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold == 2, "its me")
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! BuyEquipment("shovel")
      expectNoMessage(50.millis)
      gameActor ! Update
      gs = expectMsgType[GameState](1000.millis)
      gameStateJSON = gs.gameState
      ParseIt = Json.parse(gameStateJSON)
      Gold = (ParseIt\ "gold").as[Double]
      assert(Gold == 10.0)
    }
  }
}
