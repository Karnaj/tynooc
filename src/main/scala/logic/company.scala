package logic.company

import collection.mutable.HashSet

import logic.train._
import logic.travel._
import logic.town._
import logic.game._
import logic.world._
import logic.graph._

object PriceSimulation {
  def upgradePrice[Model <: VehicleModel](
    from: VehicleFromModel[Model],
    to: Model): Double = {
    (to.price - from.model.price) * 1.2
  }
}

final case class PathNotFoundException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** A company.
 *
 *  @param name The name of the company.
 *  @param fabricTown The town in which the company rolling stock is produced.
 */
class Company(var name: String, val fabricTown: Town) {
  /** The company trains. */
  val trains: HashSet[Train] = HashSet()
  /** The company carriages. */
  val vehicles: HashSet[Vehicle] = HashSet()

  var money: Double = 0

  /** Current travels for this company. */
  def travels: HashSet[Travel] = Game.world.travelsOf(this)

  def credit(amount: Double): Unit = money += amount

  def debit(amount: Double): Unit = {
    money -= (amount + (0.02*Math.max(0, amount-money)))
  }

  /** Returns the carriages of this company. */
  def carriages: HashSet[Carriage] = vehicles.flatMap {
    case c: Carriage => Some(c)
    case _ => None
  }

  /** Returns the available carriages of this company.
   *
   *  A carriage is available if not in a train. */
  def carriagesAvailable: HashSet[Carriage] = carriages.filter(!_.isUsed)

  /** Returns the carriages of this company available in a town. */
  def carriagesStoredAt(town: Town): HashSet[Carriage] =
    carriages.filter(c => !c.isUsed && c.town == town)

  /** Returns the engines of this company. */
  def engines: HashSet[Engine] = vehicles.flatMap {
    case c: Engine => Some(c)
    case _ => None
  }

  /** Returns the available engines of this company.
   *
   *  An engine is available if not in a train. */
  def enginesAvailable: HashSet[Engine] = engines.filter(!_.isUsed)

  /** Returns the engines of this company available in a town. */
  def enginesStoredAt(town: Town): HashSet[Engine] =
    engines.filter(c => !c.isUsed && c.town == town)

  def trainsAvailable: HashSet[Train] = trains.filter { _.isAvailable }

  def buyEngine(name: String): Unit = {
    val model = EngineModel(name)
    if (model.price <= money) {
      debit(model.price)
      vehicles.add(new Engine(model, fabricTown))
    }
  }

  def buyCarriage(name: String): Unit = {
    val model = CarriageModel(name)
    if (model.price <= money) {
      debit(model.price)
      vehicles.add(new Carriage(model, fabricTown))
    }
  }

  /** Creates a new train, with only an engine. */
  def createTrainFromEngine(engine: Engine): Train = {
    if (!ownsVehicle(engine)) {
      throw new IllegalArgumentException("Company doesn’t own the engine")
    }
    if (engine.isUsed) {
      throw new IllegalArgumentException("Engine is in use")
    }
    val train = new Train(engine, List(), engine.town)
    engine.train = Some(train)
    trains.add(train)
    train
  }

  /** Adds a carriage at the tail of an existing train. */
  def addCarriageToTrain(train: Train, carriage: Carriage): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (!ownsVehicle(carriage)) {
      throw new IllegalArgumentException("Company doesn’t own the carriage")
    }
    if (carriage.isUsed) {
      throw new IllegalArgumentException("Carriage is in use")
    }
    if (train.town != carriage.town) {
      throw new IllegalArgumentException("Train and Carriage in different locations")
    }

    train.addCarriage(carriage)
    carriage.train = Some(train)
  }

  /** Removes the tail carriage of an existing train. */
  def removeCarriageFromTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    val carriage: Carriage = train.removeCarriage()
    carriage.train = None
    carriage.town = train.town
  }

  /** Completely disassemble an existing train. */
  def disassembleTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    train.engine.town = train.town
    train.engine.train = None

    train.carriages.foreach{ c =>
      c.town = train.town
      c.train = None
    }
    trains.remove(train)
  }

  /** Start a new travel.
   *
   *  Starting town will be the train's current town.
   */
  def launchTravel(train: Train, to: Town): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (train.tooHeavy) {
      throw new IllegalArgumentException("Train is too heavy")
    }
    if (train.damaged) {
      throw new IllegalArgumentException("Train is damaged")
    }
    val routes = Game.world.findPath(train.town, to).getOrElse(throw new PathNotFoundException)
    val travel = new Travel(train, routes, this)
    train.travel = Some(travel)
    Game.world.addTravel(travel)
  }


  def repair(vehicle: Vehicle): Unit = {
    if (!ownsVehicle(vehicle)) {
      throw new IllegalArgumentException("Company doesn’t own the vehicle")
    }
    if (vehicle.isUsed) {
      throw new IllegalArgumentException("Vehicle is in use")
    }
    debit(vehicle.repairPrice)
    vehicle.repair()
  }

  def upgrade[Model <: VehicleModel](old: VehicleFromModel[Model], model: Model): Unit = {
    if (!ownsVehicle(old)) {
      throw new IllegalArgumentException("Company doesn’t own the vehicle")
    }
    if (old.isUsed) {
      throw new IllegalArgumentException("Vehicle is in use")
    }
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      debit(PriceSimulation.upgradePrice(old, model))
      old.model = model
    }
  }

  def ownsTrain(train: Train): Boolean = trains.contains(train)
  def ownsVehicle(vehicle: Vehicle): Boolean = vehicles.contains(vehicle)
}
