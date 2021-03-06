package logic.world

import logic.graph._
import logic.town._
import logic.travel._
import logic.room._
import logic.company._
import logic.route._

import collection.mutable.HashMap
import collection.mutable.HashSet

object Status {
  var _id = 0

  sealed class Val(val id: Int) {
    _id += 1
    def this() { this(_id) }
  }

  object RICH extends Status.Val
  object POOR extends Status.Val
  object WELL extends Status.Val
}

/** The game world representation.
 *
 *  The world is a graph, with vertices being towns, and edges being routes.
 *  This class handles all travels, as well as town population.
 */
class World {
  var status = Array(Status.RICH, Status.POOR, Status.WELL)
  var statusCriteria = Array((d:Town) => (r:Room) => r.comfort,
                             (d:Town) => (r:Room) => r.price(d),
                             (d:Town) => (r:Room) => r.comfort / (r.price(d)+1))
  var statusNumber = status.length
  var fuelPrice = 1

  private var _towns: HashSet[Town] = HashSet()

  private var _travels: HashSet[Travel] = HashSet()

  /** Callback called any time a new travel is added.
   *
   *  This is used to signal the new travel to the gui. */
  var onAddTravel: Travel => Unit = {_ => ()}

  /** The towns in the world. */
  def towns: HashSet[Town] = _towns

  /** The current travels in the world. */
  def travels: HashSet[Travel] = _travels

  /** Total world population. */
  def population: Int = towns.foldLeft[Int](0) { _ + _.population }

  /** Adds a new town. */
  def addTown(town: Town): Unit = _towns.add(town)

  /** Creates and add a new town. */
  def addTown(name: String, x: Double, y: Double, welcomingLevel: Double): Unit = {
    addTown(new Town(name, x, y, welcomingLevel))
  }

  /** Adds a new travel in the world. */
  def addTravel(travel:Travel): Unit = {
    _travels.add(travel)
    // callback
    onAddTravel(travel)
  }

  /** Gets all travels of a specific company in the world. */
  def travelsOf(company: Company): HashSet[Travel] =
    travels.filter { _.owner == company }


  def tryTravel(
    start: Town,
    destination: Town,
    migrantByStatus: Array[Int]): Unit = {
    val availableTravels = travels.toList.filter {
      t => t.isWaitingAt(start) && t.stopsAt(destination)
    }
    var rooms = availableTravels.flatMap { _.availableRooms }
    status.foreach { status =>
      var takenPlacesNumber = 0
      var p = migrantByStatus(status.id)
      rooms = rooms.sortBy { statusCriteria(status.id)(destination) }
      while(takenPlacesNumber < p && !rooms.isEmpty) {
        val room = rooms.head
        val nb = Math.min(p, room.availablePlaces)
        room.takePlaces(nb, destination, status)
        takenPlacesNumber += nb
        p -= nb
        if(!room.isAvailable)
          rooms = rooms.tail
      }
      start.deletePassengers(takenPlacesNumber, status, destination)
    }
  }

  /** Update the state of all travels and towns.
   *
   *  @param dt the time passed since last update step.
   */
  def update(dt: Double): Unit = {
    travels.foreach(_.update(dt))
    _travels = travels.filter(!_.isDone)
    // add later
    towns.foreach(_.update(dt))
  }

  /** Find the shortest path between two towns.
   *
   * @param from start town.
   * @param to   end town.
   * @return The list of vertices in the path, in order.
   */
  def findPath(from: Town, to: Town) : Option[List[Route]] = {
    // Dijkstra
    var closed: HashSet[Town] = new HashSet()
    var open: HashSet[Town] = new HashSet()
    var dist: HashMap[Town, Double] = new HashMap()
    var path: HashMap[Town, List[Route]] = new HashMap()
    dist(from) = 0
    open.add(from)
    path(from) = List()

    while(!open.isEmpty && !closed(to)) {
      var town: Town = open.minBy[Double](dist(_))
      town.routes.foreach { route =>
        if(!open(route.end) && !closed(route.end)) {
          open.add(route.end)
          dist(route.end) = dist(town) + route.length
          path(route.end) = route :: path(town)
        } else if(open(route.end) && dist(route.end) > dist(town) + route.length) {
          dist(route.end) = dist(town) + route.length
          path(route.end) = route :: path(town)
        }
      }
      open.remove(town)
      closed.add(town)
    }

    // path was build in reverse direction
    path.get(to) match {
      case None => None
      case Some(l) => Some(l.reverse)
    }
  }

  /** Returns the list of towns accessibles from a starting town. */
  def townsAccessibleFrom(from: Town): List[Town] = {
    var closed: Set[Town] = Set()
    var open: Set[Town] = Set(from)
    var accessibles: Set[Town] = Set()
    while (!open.isEmpty) {
      val town = open.head
      closed += town
      open -= town
      town.neighbours.foreach { n =>
        if(!closed.contains(n)) {
          open = open + n
          accessibles += n;
        }
      }
    }
    accessibles.toList
  }

  override def toString: String = {
    towns.foldLeft[String]("") { (d, t) => d + s"$t\n" }
  }
}
