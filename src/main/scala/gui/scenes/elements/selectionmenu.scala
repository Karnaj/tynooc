package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

/** A list of submenus, only one of which can be selected at a time.
 *
 *  The submenus are displayed in a vertical list. */
class SelectionMenu extends VBox(3) {
  private var group: ToggleGroup = new ToggleGroup()

  /** Add a new submenu.
   *
   *  @param text the title of the submenu.
   *  @param action a callback called when the submenu is selected.
   */
  def addMenu(text: String, action: => Unit): Unit = {
    val button = new RadioButton(text) {
      onAction = (event: ActionEvent) => action
      styleClass.remove("radio-button")
      styleClass.add("link")
    }
    group.toggles.add(button)
    children.add(button)
  }

  /** Deselect all entries. */
  def deselect(): Unit = {
    group.toggles.foreach(_.selected = false)
  }
}

/** Creates a SelectionMenu automatically from a list of objects.
 *
 *  @param list the list of objects, each of which correspond to a submenu.
 *  @param text the title of the submenus, function of the corresponding object.
 *  @param action the action of the submenus, function of the corresponding object.
 */
class SelectionList[A](
  list: List[A],
  text: A => String,
  action: A => Unit)
extends ScrollPane {
  private var menu: SelectionMenu = new SelectionMenu()
  list.foreach(a => menu.addMenu(text(a), action(a)))
  content = menu

  def deselect(): Unit = menu.deselect()
}
