//Static HashMap
object EngineModel {
  private var models : HashMap[String, EngineModel] =
    HashMap(
      ("Basic", new EngineModel(50,50,70,25,15))
      ("Advanced", new EngineModel(25,100,140,50,5))
    )
}

//Static HashMap
object CarriageModel {
  private var models : HashMap[String, CarriageModel] =
    HashMap(
      ("Basic", new EngineModel(50,50,10))
      ("Advanced", new EngineModel(50,50,15))
    )
}

/** An engine model. */
class EngineModel(
  _weight: Double,
  _power: Double,
  _speed: Double,
  _fuelCapacity: Double,
  _consumption: Double) {
    def weight: Double = _weight
    def power: Double = _power
    def speed: Double = _speed
    def fuelCapacity: Double = _fuelCapacity
    def consumption: Double = _consumption

    def apply(s: String): EngineModel = {
      return models[s]
    }
}

/** A carriage model. */
class CarriageModel(
  _weight: Double,
  _capacity: Int,
  _comfort: Double) {
    def weight: Double = _weight
    def capacity: Int = _capacity
    def comfort: Double = _comfort

    def apply(s: String): CarriageModel = {
      return models[s]
    }
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(_model: EngineModel) {
  var health: Double = 100
  var fuel: Double = model.fuelCapacity

  def model: EngineModel = _model
}

/** A carriages
 *
 *  @param _model the carriage model.
 */
class Carriage(_model: CarriageModel) {
  var health: Double = 100

  def model: CarriageModel = _model
}


class Train (e: Engine, c: List[Carriage]) {
  var engine: Engine = e
  var carriages: List[Carriage] = c

  def weight: Double = {
    (if (engine == null) 0 else engine.model.weight)
    + carriages.foldLeft[Double](0) { (acc, v) => acc + v.model.weight }
  }
}
