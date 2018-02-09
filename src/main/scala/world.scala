/** World representation */
class World extends PositionWeightedGraph[Double] {
  /** A town in the world.
   * @param name the town name.
   * @param xPos the town x coordonate.
   * @param yPos the town y coordonate. */
  class Town(name: String, xPos: Double, yPos: Double)
  extends PositionWeightedVertice {
    /** A route starting from this town.
     * @param _destination the route destination town.
     * @param _length the route length. */
    class Route(_destination: Town, _length: Double) {
      /** The route destination. */
      val destination: Town = _destination
      /** The route length. */
      val length: Double = _length
    }

    /** The town x coordonate in the world. */
    val x: Double = xPos
    /** The town y coordonate in the world. */
    val y: Double = yPos


    private var _routes: List[Route] = List()
    /** The list of routes. */
    def routes: List[Route] = _routes
    /** Adds a route.
     * @param newRoute the route to add. */
    def addRoute(newRoute: Route): Unit = {
      _routes = newRoute::_routes
    }

    /** Iterate over all adjacent edges.
     * @param action the function called for each edge.
     *  takes the destination Vertice and the edge weight as parameters. */
    def iterateWeightedEdges(action: (Vertice, Double)=>Unit): Unit = {
      this.routes.foreach((route: Route) => action(route.destination, route.length))
    }
  }

  private var _towns: List[Town] = List()
  /** The list of towns. */
  def towns: List[Town] = _towns
  /** Adds a town.
   * @param newTown the town to add. */
  def addTown(newTown: Town): Unit = {
    _towns = newTown::_towns
  }
}
