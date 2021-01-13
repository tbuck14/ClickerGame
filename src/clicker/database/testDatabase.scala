package clicker.database

import play.api.libs.json.{JsValue, Json}

class testDatabase extends Database {
  var userMap: Map[String, JsValue] = Map()
  override def playerExists(username: String): Boolean = {
    var TF: Boolean = false
    if(userMap.contains(username)){TF = true }; TF
  }
  override def createPlayer(username: String): Unit = {
    val ShovelMap: Map[String, JsValue] = Map("id"-> Json.toJson("shovel"), "name"->Json.toJson("Shovel"),"numberOwned"->Json.toJson(0),"cost"->Json.toJson(10))
    val ExcavatorMap: Map[String, JsValue] = Map("id"-> Json.toJson("excavator"), "name"->Json.toJson("Excavator"),"numberOwned"->Json.toJson(0),"cost"->Json.toJson(200))
    val MineMap: Map[String, JsValue] = Map("id"-> Json.toJson("mine"), "name"->Json.toJson("Mine"),"numberOwned"->Json.toJson(0),"cost"->Json.toJson(1000))
    val EquipmentMap: Map[String, JsValue] = Map("shovel"-> Json.toJson(ShovelMap), "excavator"-> Json.toJson(ExcavatorMap), "mine"-> Json.toJson(MineMap))
    val CInfo: Map[String, JsValue] = Map("username"-> Json.toJson(username),"gold"-> Json.toJson(0), "lastUpdateTime"->Json.toJson(System.nanoTime()),"equipment"-> Json.toJson(EquipmentMap))
    userMap += (username -> Json.toJson(CInfo))
  }
  override def saveGameState(username: String, gameState: String): Unit = {
    userMap -= username
    userMap += username -> Json.toJson(gameState)
  }

  override def loadGameState(username: String): String = {
    Json.stringify(userMap(username))
  }
}
