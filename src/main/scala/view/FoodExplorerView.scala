package view

import scalafx.Includes._
import scalafx.animation.{Interpolator, TranslateTransition}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Label, ListView, Separator}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Font
import scalafx.util.Duration

import model.{Food, NutrientProfile}

// for optional icon fields on Food (iconPath, iconFileName, imagePath)
import scala.util.Try
import scala.reflect.Selectable.reflectiveSelectable

final class FoodExplorerView(foods: Seq[Food]) extends BorderPane {

  // -----------------------
  // Header (top-left)
  // -----------------------
  private val headerBar = new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(10, 10, 6, 10)
    children = Seq(
      new Label("Food Explorer") {
        styleClass += "label-title"
        font = Font.font("Poppins", 20)
      }
    )
  }
  top = headerBar

  // -----------------------
  // Data
  // -----------------------
  private val foodsBuffer: ObservableBuffer[Food] = ObservableBuffer(foods*)

  // -----------------------
  // ListView (center)
  // -----------------------
  private val listView: ListView[Food] = new ListView[Food](foodsBuffer)

  // Use JavaFX cell factory explicitly to avoid overload ambiguity.
  {
    import javafx.scene as jfxs
    import javafx.scene.control as jfxc
    import javafx.scene.image as jfxi
    import javafx.scene.layout as jfxl
    import javafx.util.Callback

    listView.delegate.setCellFactory(new Callback[jfxc.ListView[Food], jfxc.ListCell[Food]] {
      override def call(lv: jfxc.ListView[Food]): jfxc.ListCell[Food] =
        new jfxc.ListCell[Food] {
          override def updateItem(item: Food, empty: Boolean): Unit = {
            super.updateItem(item, empty)
            if (empty || item == null) {
              setText(null)
              setGraphic(null)
            } else {
              val nameLabel = new jfxc.Label(item.name)
              nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700;")

              // Resolve icon path (iconPath | iconFileName | imagePath)
              val maybePath: Option[String] =
                Try(item.asInstanceOf[{ def iconPath: String }].iconPath).toOption
                  .orElse(Try(item.asInstanceOf[{ def iconFileName: String }].iconFileName).toOption)
                  .orElse(Try(item.asInstanceOf[{ def imagePath: String }].imagePath).toOption)

              val iconViewOpt: Option[jfxi.ImageView] = maybePath.flatMap { raw =>
                val p = if (raw.startsWith("/")) raw else s"/images/foods/$raw"
                val is = getClass.getResourceAsStream(p)
                if (is != null) {
                  val iv = new jfxi.ImageView(new jfxi.Image(is))
                  iv.setFitHeight(22); iv.setFitWidth(22); iv.setPreserveRatio(true)
                  Some(iv)
                } else None
              }

              val row = new jfxl.HBox(10.0)
              jfxl.HBox.setHgrow(nameLabel, jfxl.Priority.ALWAYS)
              iconViewOpt match
                case Some(iv) => row.getChildren.addAll(nameLabel, iv) // icon on the right
                case None     => row.getChildren.add(nameLabel)

              row.getStyleClass.add("food-cell")
              row.setOnMouseEntered(_ => row.getStyleClass.add("food-cell-hover"))
              row.setOnMouseExited(_  => row.getStyleClass.remove("food-cell-hover"))

              setText(null)
              setGraphic(row)
            }
          }
        }
    })
  }

  // Hide scrollbars when the ListView is added to a Scene
  listView.sceneProperty.onChange { (_, _, s) =>
    if (s != null) {
      val bars /*: scala.collection.Set[javafx.scene.Node]*/ = listView.lookupAll(".scroll-bar")
      // It's a Scala Set here -> use iterator (no parentheses)
      val it = bars.iterator
      while (it.hasNext) {
        val n = it.next()
        n.setVisible(false)
        n.setManaged(false)
      }
    }
  }

  // NOTE: Removed the custom onScroll handler that consumed the event and
  // manually moved selection. Let the ListView handle scrolling natively.
  // (Nothing else changed.)

  // Click or Enter opens the drawer
  listView.onMouseClicked = _ => {
    val f = listView.selectionModel().getSelectedItem
    if (f != null) openDrawer(f)
  }
  listView.onKeyPressed = (ke: KeyEvent) => {
    if (ke.code == KeyCode.Enter) {
      val f = listView.selectionModel().getSelectedItem
      if (f != null) { openDrawer(f); ke.consume() }
    }
  }

  // -----------------------
  // Drawer at right
  // -----------------------
  private val drawerWidth = 360.0
  private val drawerPane  = new VBox {
    styleClass ++= Seq("card", "drawer")
    padding = Insets(16)
    spacing = 10
    prefWidth = drawerWidth
    maxWidth  = drawerWidth
    translateX = drawerWidth // start off-screen to the right
  }

  // Overlay to dim when drawer is open
  private val overlay = new Rectangle {
    fill = Color.rgb(0, 0, 0, 0.28)
    visible = false
    managed = false
    mouseTransparent = false
  }

  // Stack: list (base), overlay, drawer aligned right
  private val stack = new StackPane {
    children = Seq(listView, overlay, drawerPane)
  }
  StackPane.setAlignment(drawerPane, Pos.CenterRight)
  center = stack

  // Keep overlay sized to stack
  stack.widthProperty.onChange((_, _, w) => overlay.width = w.doubleValue)
  stack.heightProperty.onChange((_, _, h) => overlay.height = h.doubleValue)

  // Close drawer by clicking overlay or pressing ESC
  overlay.onMouseClicked = _ => closeDrawer()
  this.onKeyPressed = (ke: KeyEvent) => {
    if (ke.code == KeyCode.Escape && isDrawerOpen) { closeDrawer(); ke.consume() }
  }

  private def isDrawerOpen: Boolean = drawerPane.translateX.value == 0.0

  private def openDrawer(f: Food): Unit = {
    renderDrawer(f)
    overlay.visible = true; overlay.managed = true
    animateDrawer(to = 0.0)
  }

  private def closeDrawer(): Unit = {
    animateDrawer(to = drawerWidth, after = () => { overlay.visible = false; overlay.managed = false })
  }

  private def animateDrawer(to: Double, after: () => Unit = () => ()): Unit = {
    new TranslateTransition {
      node = drawerPane
      duration = Duration(220)
      toX = to
      interpolator = Interpolator.EaseOut
      onFinished = _ => after()
    }.play()
  }

  // -----------------------
  // Drawer content
  // -----------------------
  private def miniRow(name: String, value: String): HBox =
    new HBox {
      spacing = 8
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(name)  { style = "-fx-font-weight: 700; -fx-text-fill:#4E342E;" },
        new Label(value) { style = "-fx-text-fill:#6d5c54;" }
      )
    }

  private def renderDrawer(f: Food): Unit = {
    // Clear via JavaFX delegate to avoid sfx/jfx type mixups
    drawerPane.delegate.getChildren.clear()

    val title = new Label(f.name) {
      font = Font.font("Poppins", 18)
      style = "-fx-font-weight: 700; -fx-text-fill:#E65100;"
    }

    val maybePath: Option[String] =
      Try(f.asInstanceOf[{ def iconPath: String }].iconPath).toOption
        .orElse(Try(f.asInstanceOf[{ def iconFileName: String }].iconFileName).toOption)
        .orElse(Try(f.asInstanceOf[{ def imagePath: String }].imagePath).toOption)

    val iconView: Option[ImageView] = maybePath.flatMap { raw =>
      val p = if (raw.startsWith("/")) raw else s"/images/foods/$raw"
      Option(getClass.getResourceAsStream(p)).map { is =>
        new ImageView(new Image(is)) { fitHeight = 28; fitWidth = 28; preserveRatio = true }
      }
    }

    val header = new HBox {
      alignment = Pos.CenterLeft
      spacing = 10
      children = iconView match
        case Some(iv) =>
          HBox.setHgrow(title, Priority.Always)
          Seq(title, iv)               // icon on the right
        case None => Seq(title)
    }

    val n: NutrientProfile = f.nutrients
    val rows: Seq[scalafx.scene.Node] = Seq(
      new Label("Nutrients (per 100g)") { styleClass += "label-section" },

      miniRow("Calories",  f"${n.calories}%.0f kcal"),
      miniRow("Protein",   f"${n.protein}%.1f g"),
      miniRow("Carbs",     f"${n.carbs}%.1f g"),
      miniRow("Fat",       f"${n.fat}%.1f g"),

      new Separator,
      miniRow("Vitamin A", f"${n.vitaminA}%.1f mg"),
      miniRow("Vitamin C", f"${n.vitaminC}%.1f mg"),
      miniRow("Iron",      f"${n.iron}%.1f mg"),
      miniRow("Calcium",   f"${n.calcium}%.0f mg"),
      miniRow("Potassium", f"${n.potassium}%.0f mg"),
      miniRow("Magnesium", f"${n.magnesium}%.0f mg"),
      miniRow("Sodium",    f"${n.sodium}%.0f mg"),
      miniRow("Fiber",     f"${n.fiber}%.1f g")
    )

    // add via JavaFX delegate to avoid Node type mismatch
    drawerPane.delegate.getChildren.add(header.delegate)
    rows.foreach(n => drawerPane.delegate.getChildren.add(n.delegate))
  }
}
