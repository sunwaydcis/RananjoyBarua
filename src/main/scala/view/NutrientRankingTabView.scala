package view

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{ComboBox, Label, ListCell, ListView}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.scene.text.Font
import scalafx.Includes._

import model.Food
import service.FoodDatabase

final class NutrientRankingTabView(foodDB: FoodDatabase) extends VBox {
  spacing = 10
  padding = Insets(10)

  private val nutrientDisplay = ObservableBuffer(
    "Protein","Carbs","Fat","Calories",
    "vitaminA","vitaminC","Iron","Calcium",
    "Potassium","Magnesium","Sodium","Fiber"
  )

  private val picker = new ComboBox[String](nutrientDisplay) {
    value = "Protein"
    prefWidth = 220
  }

  // List holds (Food, numeric value) so we can render rich cells
  private val list = new ListView[(Food, Double)] {
    // Hide scrollbars with CSS
    style = "-fx-background-insets: 0; -fx-padding: 0;"
    fixedCellSize = 40 // makes items consistent height
  }

  // --- icon loader: uses Food.imagePath (e.g. "/images/foods/egg.png") ---
  private def iconFor(f: Food): Option[ImageView] = {
    val p = Option(f.imagePath).getOrElse("").trim
    if (p.isEmpty) None
    else {
      val cp = if (p.startsWith("/")) p else s"/images/foods/$p"
      Option(getClass.getResource(cp)).map { url =>
        new ImageView(new Image(url.toExternalForm)) {
          fitWidth = 22
          fitHeight = 22
          preserveRatio = true
          smooth = true
        }
      }
    }
  }

  // --- custom cell: bold, larger name + value on left, icon on right ---
  list.cellFactory = { (_: ListView[(Food, Double)]) =>
    new ListCell[(Food, Double)] {
      item.onChange { (_, _, it) =>
        if (it == null) { text = null; graphic = null }
        else {
          val (food, value) = it

          val nameLbl = new Label(food.name) {
            font = Font.font("Poppins", 16)
            style = "-fx-font-weight: 700;"
          }
          val valueLbl = new Label(f"${value}%.2f") {
            style = "-fx-text-fill:#6d5c54;"
          }

          val left = new HBox {
            spacing = 8
            alignment = Pos.CenterLeft
            children = Seq(nameLbl, valueLbl)
          }
          HBox.setHgrow(left, Priority.Always)

          val rowChildren: Seq[Node] = iconFor(food) match {
            case Some(iv) => Seq(left, iv) // icon on right
            case None     => Seq(left)
          }

          val row = new HBox {
            spacing = 10
            alignment = Pos.CenterLeft
            children = rowChildren
          }

          text = null
          graphic = row
        }
      }
    }
  }

  private def refresh(): Unit = {
    val nutrient = picker.value.value
    val ranked   = foodDB.rankFoodsBy(nutrient) // Seq[(Food, Double)]
    list.items   = ObservableBuffer(ranked*)
  }

  picker.onAction = _ => refresh()
  refresh()

  children = Seq(
    new Label("Rank by nutrient") { styleClass += "label-title" },
    picker,
    list
  )
}
