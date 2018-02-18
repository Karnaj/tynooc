/* Main game display */

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._

class GameScene(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn = new Button("Menu")
  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  root = new BorderPane(
    new WorldPane,
    new MenuPane(menuBtn),
    new TrainPane,
    new TownPane,
    new PlayerPane,
  )
}
