package clicker.model

import play.api.libs.json.{JsValue, Json}

class BEnd {
  var Gold: Double = 0
  var username: String = ""
  var Shovels: Int = 0
  var ShovelCost: Double = 10
  var ExcavatorCost: Double = 200
  var Excavators: Int = 0
  var MineCost: Double = 1000
  var Mines: Int = 0
  var LastUpdate: Double = System.nanoTime()
  var multiplyTime: Double = 0
  val SecondConversion: Double = 1000000000

  var StartTime: Long = System.nanoTime()
  def GoldClicked(): Unit = {Gold += 1 +(Shovels + (Excavators * 5))}
  def Idle(time: Double): Unit = {
    Gold += (time*10.0*Excavators)+(time*100.0*Mines)
  }
  def Purchase(EQ_ID: String): Unit ={ EQ_ID match{
    case "shovel" =>
      if(Gold >= ShovelCost){Shovels += 1;Gold -= ShovelCost;ShovelCost += (ShovelCost*.05)}
    case "excavator" =>
      if(Gold >= ExcavatorCost){Excavators += 1;Gold -= ExcavatorCost;ExcavatorCost += (ExcavatorCost*.1)}
    case "mine" =>
      if(Gold >= MineCost){Mines+= 1;Gold -= MineCost;MineCost += (MineCost*.1)}
  }
  }
  def IsUpdate(): String = {
    multiplyTime = (System.nanoTime() - LastUpdate)/SecondConversion
    LastUpdate = System.nanoTime()
    Idle(multiplyTime)
    CreateJson()
  }
  def CreateJson(): String = {
    val ShovelMap: Map[String, JsValue] = Map("id"-> Json.toJson("shovel"), "name"->Json.toJson("Shovel"),"numberOwned"->Json.toJson(Shovels),"cost"->Json.toJson(ShovelCost))
    val ExcavatorMap: Map[String, JsValue] = Map("id"-> Json.toJson("excavator"), "name"->Json.toJson("Excavator"),"numberOwned"->Json.toJson(Excavators),"cost"->Json.toJson(ExcavatorCost))
    val MineMap: Map[String, JsValue] = Map("id"-> Json.toJson("mine"), "name"->Json.toJson("Mine"),"numberOwned"->Json.toJson(Mines),"cost"->Json.toJson(MineCost))
    val EquipmentMap: Map[String, JsValue] = Map("shovel"-> Json.toJson(ShovelMap), "excavator"-> Json.toJson(ExcavatorMap), "mine"-> Json.toJson(MineMap))
    val FinalMap: Map[String, JsValue] = Map("username"-> Json.toJson(username), "gold"->Json.toJson(Gold), "lastUpdateTime"->Json.toJson(LastUpdate),"equipment"-> Json.toJson(EquipmentMap))
    val FinalMapToJson: JsValue = Json.toJson(FinalMap)
    Json.stringify(FinalMapToJson)
  }
  def loadGame(LoadThisJson: String): Unit = {
    val ParseIt: JsValue = Json.parse(LoadThisJson)
    Gold = (ParseIt \ "gold").as[Double]
    username = (ParseIt \ "username").as[String]
    Shovels = (ParseIt\"equipment"\"shovel"\"numberOwned").as[Int]
    ShovelCost = (ParseIt\"equipment"\"shovel"\"cost").as[Double]
    Excavators = (ParseIt\"equipment"\"excavator"\"numberOwned").as[Int]
    ExcavatorCost = (ParseIt\"equipment"\"excavator"\"cost").as[Double]
    Mines = (ParseIt\"equipment"\"mine"\"numberOwned").as[Int]
    MineCost = (ParseIt\"equipment"\"mine"\"cost").as[Double]
    LastUpdate = (ParseIt\"lastUpdateTime").as[Long]
  }
}
