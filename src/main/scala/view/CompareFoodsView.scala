package view

import scalafx.scene.layout.{VBox, GridPane, HBox, Priority}
import scalafx.scene.control.{Label, ListCell, ListView, SelectionMode}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.text.Font
import scalafx.Includes._

import model.Food
import service.FoodDatabase

import scala.util.Try
import scala.reflect.Selectable.reflectiveSelectable // enables structural type access

final class CompareFoodsView(foodDB: FoodDatabase) extends VBox {
  spacing = 10
  padding = Insets(10)

  // Use Food objects directly so we can render icons
  private val foodsBuf: ObservableBuffer[Food] = ObservableBuffer(foodDB.getAll*)

  // Known icons for your default foods (fallback if Food has no iconPath/imagePath)
  private val nameIconFallback: Map[String, String] = Map(
    "Egg (boiled)"        -> "/images/foods/egg.png",
    "Whole Wheat Bread"   -> "/images/foods/bread.png",
    "Avocado"             -> "/images/foods/avocado.png",
    "Chicken Breast"      -> "/images/foods/chicken.png",
    "Brown Rice"          -> "/images/foods/rice.png",
    "Spinach"             -> "/images/foods/spinach.png",
    "Tomato"              -> "/images/foods/tomato.png",
    "Cheddar Cheese"      -> "/images/foods/cheese.png",
    "Apple"               -> "/images/foods/apple.png",
    "Olive Oil"           -> "/images/foods/oliveoil.png"
  )

  private def slugify(s: String): String =
    s.toLowerCase
      .replaceAll("\\(.*?\\)", "")  // remove parenthetical
      .replaceAll("[^a-z0-9]+", "_")
      .replaceAll("^_+|_+$", "")

  /** Best-effort icon path extractor for a Food, returning a classpath URL string if found. */
  private def iconUrlFor(f: Food): Option[String] = {
    // 1) Try structural fields if present: iconPath / imagePath
    val pathOpt: Option[String] =
      Try(f.asInstanceOf[{ def iconPath: String }].iconPath).toOption
        .orElse(Try(f.asInstanceOf[{ def imagePath: String }].imagePath).toOption)
        .orElse(nameIconFallback.get(f.name))

    // 2) If we have a path, normalize to our /images/foods/... if relative
    val candidatePaths: Seq[String] = pathOpt match {
      case Some(p) if p.startsWith("/") => Seq(p)
      case Some(p)                      => Seq(s"/images/foods/$p", s"/$p")
      case None =>
        val slug = slugify(f.name)
        Seq(
          s"/images/foods/$slug.png",
          s"/images/foods/$slug.jpg",
          s"/images/foods/$slug.jpeg"
        )
    }

    candidatePaths.view
      .map(getClass.getResource)       // try as classpath resource
      .collectFirst { case url if url != null => url.toExternalForm }
  }

  private val list = new ListView[Food](foodsBuf) {
    selectionModel().selectionMode = SelectionMode.Multiple

    // Custom cell: bold, larger text + icon on the RIGHT
    cellFactory = (_: ListView[Food]) => new ListCell[Food] {
      item.onChange { (_, _, f) =>
        if (f == null) { text = null; graphic = null }
        else {
          val name = new Label(f.name) {
            font = Font.font("Poppins", 16)     // bigger
            style = "-fx-font-weight: 700;"     // bold
          }

          val iconViewOpt: Option[ImageView] =
            iconUrlFor(f).map { url =>
              new ImageView(new Image(url, true)) {
                fitHeight = 22
                fitWidth  = 22
                preserveRatio = true
                smooth = true
              }
            }

          val row = new HBox {
            spacing = 10
            children = iconViewOpt match
              case Some(iv) =>
                HBox.setHgrow(name, Priority.Always)
                Seq(name, iv)                      // icon on the RIGHT
              case None =>
                Seq(name)
          }

          text = null
          graphic = row
        }
      }
    }
  }

  private val grid = new GridPane { hgap = 12; vgap = 6 }

  // Recompute compare grid whenever selection changes
  list.selectionModel().selectedItems.onChange {
    val selectedFoods = list.selectionModel().getSelectedItems.toSeq
    renderCompare(selectedFoods)
  }

  private def renderCompare(fds: Seq[Food]): Unit = {
    grid.children.clear()
    if (fds.isEmpty) {
      grid.add(new Label("Select foods to compare."), 0, 0); return
    }

    // Header row
    grid.add(new Label("Nutrient") { style = "-fx-font-weight: 700;" }, 0, 0)
    fds.zipWithIndex.foreach { case (f, i) =>
      grid.add(new Label(f.name) { style = "-fx-font-weight: 700;" }, i + 1, 0)
    }

    def row(r: Int, label: String, get: Food => Double, suffix: String = ""): Unit = {
      grid.add(new Label(label), 0, r)
      fds.zipWithIndex.foreach { case (f, i) =>
        grid.add(new Label(s"${get(f)}$suffix"), i + 1, r)
      }
    }

    var r = 1
    row(r, "Calories", _.nutrients.calories, " kcal"); r += 1
    row(r, "Protein",  _.nutrients.protein,  " g");    r += 1
    row(r, "Carbs",    _.nutrients.carbs,    " g");    r += 1
    row(r, "Fat",      _.nutrients.fat,      " g");    r += 1
    row(r, "Vitamin A",_.nutrients.vitaminA, " mg");   r += 1
    row(r, "Vitamin C",_.nutrients.vitaminC, " mg");   r += 1
    row(r, "Iron",     _.nutrients.iron,     " mg");   r += 1
    row(r, "Calcium",  _.nutrients.calcium,  " mg");   r += 1
    row(r, "Potassium",_.nutrients.potassium," mg");   r += 1
    row(r, "Magnesium",_.nutrients.magnesium," mg");   r += 1
    row(r, "Sodium",   _.nutrients.sodium,   " mg");   r += 1
    row(r, "Fiber",    _.nutrients.fiber,    " g");    r += 1
  }

  children = Seq(
    new Label("Compare Foods") { styleClass += "label-title" },
    list,
    grid
  )
}
