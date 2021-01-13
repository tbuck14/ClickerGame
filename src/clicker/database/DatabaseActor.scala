package clicker.database

import akka.actor.Actor
import clicker.{GameState, SaveGame, StartedGame}

/***
  * @param dbType Indicates the type of database to be used. Use "mySQL" to connect to a MySQL server, or "test" to
  *               use data structures in a new class that extends the Database trait.
  */
class DatabaseActor(dbType: String) extends Actor {

  val database: Database = dbType match {
    case "mySQL" => new MySQLDatabase()
    case "test" => new testDatabase()
  }

  override def receive: Receive = {
    case SaveGame(username: String, gameState: String) => database.saveGameState(username,gameState)
    case StartedGame(username: String) =>
      if(database.playerExists(username)){val loaded: String = database.loadGameState(username);sender()!GameState(loaded)}
      else{database.createPlayer(username)}
  }

}
