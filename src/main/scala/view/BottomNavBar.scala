package view

import scalafx.scene.control.{Button, Tooltip}
import scalafx.scene.layout.{HBox, Priority, Region}
import scalafx.geometry.Insets
import scalafx.Includes._

final class BottomNavBar(onSelect: String => Unit, current: String) extends HBox {
  spacing = 12
  padding = Insets(10)
  styleClass += "bottom-nav"

  private def tabBtn(tabId: String, label: String, tip: String): Button = {
    val b = new Button(label) {
      styleClass ++= Seq("nav-button")
      tooltip = new Tooltip(tip)
      // explicit println so you see clicks in the sbt console
      onAction = _ => {
        println(s"[BottomNavBar] click -> $tabId")
        onSelect(tabId)
      }
      // make sure it’s clickable even with tight CSS
      pickOnBounds = true
      focusTraversable = false
    }
    if (tabId == current) b.styleClass += "nav-icon-active"
    b
  }

  children = Seq(
    tabBtn("dashboard", "🏠 Dashboard", "Overview"),
    tabBtn("food",      "🍎 Foods",     "Explore foods"),
    tabBtn("recipes",   "🍽 Recipes",   "Browse recipes"),
    tabBtn("compare",   "⚖ Compare",    "Compare foods"),
    tabBtn("journal",   "📓 Journal",   "Your journal"),
    tabBtn("nutrients", "🧪 Nutrients",  "Rank by nutrient")
  )

  // optional spacer to flex layout if you ever need it
  HBox.setHgrow(new Region { prefWidth = 0 }, Priority.Always)
}
