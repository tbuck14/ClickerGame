package clicker.model

import akka.actor.{Actor, ActorRef}
import clicker.{BuyEquipment, ClickGold, GameState, Save, SaveGame, StartedGame, Update}

class GameActor(username: String, database: ActorRef) extends Actor {

  var MyBackEnd: BEnd = new BEnd
  MyBackEnd.username = username
  database ! StartedGame(username)
  override def receive: Receive = {
    case ClickGold => MyBackEnd.GoldClicked()
    case BuyEquipment(equipmentId: String) => MyBackEnd.Purchase(equipmentId)
    case Update => sender() !  GameState(MyBackEnd.IsUpdate())
    case Save => database ! SaveGame(username, MyBackEnd.IsUpdate())
    case GameState(gameState: String) => MyBackEnd.loadGame(gameState)
  }

}
