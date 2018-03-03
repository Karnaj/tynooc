package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.image._
import scalafx.event._

import gui.draw._
import gui.MainStage
import logic.game._

class TopMenu(sceneModifier: MainStage.States.Val => Unit)
extends BorderPane with Drawable {
  left = new HBox {
    children = List(
      new Button ("Quit") {
        onAction =
          (event: ActionEvent) => sceneModifier(MainStage.States.MainMenu)
      }
    )
  }
  center = new HBox {
    children = List()
  }

  val timeLabel = new Label()

  val pauseButton = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/pause.png")}
    onAction = (event: ActionEvent) => Game.paused = selected()
  }

  val timeGroup: ToggleGroup = new ToggleGroup()
  val buttonX1 = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/time1.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 1
    fire()
  }
  val buttonX2 = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/time2.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 3
  }
  val buttonX4 = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/time3.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 8
  }
  timeGroup.toggles.addAll(buttonX1, buttonX2, buttonX4)

  right = new HBox {
    children = List(
      timeLabel,
      pauseButton,
      buttonX1,
      buttonX2,
      buttonX4)
  }

  override def draw(): Unit = {
    timeLabel.text = Game.timeToDateString(Game.time)
  }
}
