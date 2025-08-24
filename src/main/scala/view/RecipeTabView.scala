package view

import scalafx.scene.layout._
import scalafx.scene.control.{Label, Tooltip, Separator, ScrollPane, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import scalafx.scene.text.Font
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.chart.PieChart
import scalafx.geometry.{Insets, Pos}
import scalafx.animation.{Interpolator, TranslateTransition}
import scalafx.util.Duration
import scalafx.Includes._

import scalafx.scene.Node
import model.{Recipe, NutrientProfile}
import service.RecipeDatabase

final class RecipeTabView(recipeDB: RecipeDatabase) extends BorderPane {

  // ==== data ====
  private val allRecipes: Seq[Recipe] = recipeDB.getAll

  // ---- token helpers for search ----
  private def tokens(s: String): Array[String] =
    Option(s).map(_.toLowerCase.trim.split("\\s+").filter(_.nonEmpty)).getOrElse(Array.empty)

  /** Match query tokens as a prefix sequence of recipe-name tokens (whole-word, order-aware). */
  private def nameMatches(query: String, name: String): Boolean = {
    val q = tokens(query)
    if (q.isEmpty) return true
    val n = tokens(name)
    if (q.length > n.length) return false
    q.indices.forall { i =>
      n(i).startsWith(q(i)) // prefix match for ith word
    }
  }

  private def filterRecipes(q: String): Seq[Recipe] =
    if (q.trim.isEmpty) allRecipes else allRecipes.filter(r => nameMatches(q, r.name))

  // ==== search bar ====
  private val searchField = new TextField {
    promptText = "Search recipes"
    prefWidth = 420
  }

  private val spacer = new Region()
  HBox.setHgrow(spacer, Priority.Always)

  private val searchBar = new HBox {
    spacing = 10
    padding = Insets(10, 10, 0, 10)
    alignment = Pos.Center
    children = Seq(
      new Label("Recipes") { styleClass += "label-title" },
      spacer,
      searchField
    )
  }

  // ==== grid of recipe cards (image + name) ====
  private val grid = new FlowPane {
    hgap = 16
    vgap = 16
    padding = Insets(16)
    alignment = Pos.TopCenter
  }

  /** Make the grid scrollable (vertical) */
  private val scroller = new ScrollPane {
    content = grid
    fitToWidth = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    padding = Insets(0)
    style = "-fx-background-color: transparent; -fx-background-insets: 0;"
  }

  // robust image loader (classpath or filesystem)
  private def loadImage(path: String): Option[Image] = {
    if (path == null || path.trim.isEmpty) return None
    val candidates: Seq[String] =
      if (path.startsWith("/")) Seq(path) else Seq(s"/$path", s"/images/recipes/$path")

    candidates.view.flatMap { p =>
      val url = getClass.getResource(p)
      if (url != null) Some(new Image(url.toExternalForm, true))
      else {
        val is = getClass.getResourceAsStream(p)
        if (is != null) Some(new Image(is)) else None
      }
    }.headOption.orElse({
      import java.io.File
      val f = new File(path)
      if (f.exists()) Some(new Image(f.toURI.toString, true)) else None
    })
  }

  private def recipeThumb(path: String, w: Double, h: Double): Node =
    loadImage(path) match {
      case Some(img) =>
        new ImageView(img) {
          fitWidth = w; fitHeight = h; preserveRatio = true; smooth = true
          styleClass += "recipe-thumb"
        }
      case None =>
        new VBox {
          minWidth = w; maxWidth = w; prefWidth = w
          minHeight = h; maxHeight = h; prefHeight = h
          alignment = Pos.Center
          style =
            "-fx-background-color:#FDECEA; -fx-background-radius:8; " +
            "-fx-border-color:#F5C2C7; -fx-border-radius:8;"
          children = new Label(s"Image not found:\n$path") {
            style = "-fx-text-fill:#B42318; -fx-font-size:11px; -fx-alignment:center;"
            wrapText = true
            maxWidth = w - 12
          }
        }
    }

  private def rebuildGrid(rs: Seq[Recipe]): Unit = {
    grid.children.clear()
    rs.foreach { r =>
      val thumb: Node = recipeThumb(r.imagePath, 160, 112)
      Tooltip.install(thumb, new Tooltip(r.name))
      val card = new VBox {
        alignment = Pos.Center
        spacing = 6
        children = Seq(
          thumb,
          new Label(r.name) { font = Font.font("Poppins", 14) }
        )
        styleClass += "card"
      }
      card.onMouseClicked = _ => openDrawer(r)
      grid.children += card
    }
  }

  // initial population
  rebuildGrid(allRecipes)

  // live search
  searchField.text.onChange { (_, _, newText) =>
    rebuildGrid(filterRecipes(Option(newText).getOrElse("")))
  }

  // ==== right-side drawer ====
  private val drawerWidth = 380.0
  private val drawerPane = new VBox {
    styleClass ++= Seq("card", "drawer")
    padding = Insets(16)
    spacing = 10
    prefWidth = drawerWidth
    maxWidth = drawerWidth
  }
  private val overlay = new Rectangle {
    fill = Color.rgb(0, 0, 0, 0.28)
    visible = false
    managed = false
    mouseTransparent = false
  }
  private val stack = new StackPane {
    children = Seq(scroller, overlay, drawerPane)
  }
  StackPane.setAlignment(drawerPane, Pos.CenterRight)

  top = searchBar
  center = stack

  // overlay track sizing
  stack.widthProperty.onChange((_, _, w) => overlay.width = w.doubleValue)
  stack.heightProperty.onChange((_, _, h) => overlay.height = h.doubleValue)

  // hide drawer initially
  drawerPane.translateX = drawerWidth
  overlay.onMouseClicked = _ => closeDrawer()

  // ESC to close
  stack.onKeyPressed = ke => {
    if (ke.code == KeyCode.Escape && isDrawerOpen) { closeDrawer(); ke.consume() }
  }

  private def isDrawerOpen: Boolean = drawerPane.translateX.value == 0.0

  private def openDrawer(r: Recipe): Unit = {
    renderDrawer(r)
    overlay.visible = true; overlay.managed = true
    animateDrawer(0.0)
    stack.requestFocus()
  }
  private def closeDrawer(): Unit =
    animateDrawer(drawerWidth, after = () => { overlay.visible = false; overlay.managed = false })

  private def animateDrawer(to: Double, after: () => Unit = () => ()): Unit =
    new TranslateTransition {
      node = drawerPane
      duration = Duration(220)
      toX = to
      interpolator = Interpolator.EaseOut
      onFinished = _ => after()
    }.play()

  // ---- total nutrients for a recipe ----
  private def totalOf(r: Recipe): NutrientProfile =
    r.ingredients
      .map { case (food, grams) => food.nutrients.scaleTo(grams) }
      .foldLeft(NutrientProfile.zero)(_ + _)

  // ---- drawer content ----
  private def renderDrawer(r: Recipe): Unit = {
    drawerPane.children.clear()

    val headerLbl = new Label(r.name) {
      font = Font.font("Poppins", 18)
      style = "-fx-font-weight: 700; -fx-text-fill:#E65100;"
    }

    val ingredientsLabel = new Label("Ingredients") { styleClass += "label-section" }
    val ingredientList = new VBox {
      spacing = 4
      children = r.ingredients.map { case (f, amt) => new Label(s"$amt g ${f.name}") }
    }

    val sep1 = new Separator

    val stepsLabel = new Label("Steps") { styleClass += "label-section" }
    val stepsBox = new VBox {
      spacing = 6
      children = r.steps.zipWithIndex.map { case (st, i) =>
        new Label(s"${i + 1}. $st") { wrapText = true }
      }
    }

    val sep2 = new Separator

    val total = totalOf(r)

    val macroRow = new HBox {
      alignment = Pos.Center
      spacing = 16
      children = Seq(
        new Label(f"Protein: ${total.protein}%.1f g") { style = "-fx-font-weight: 700; -fx-text-fill:#4E342E;" },
        new Label(f"Carbs: ${total.carbs}%.1f g")     { style = "-fx-font-weight: 700; -fx-text-fill:#4E342E;" },
        new Label(f"Fat: ${total.fat}%.1f g")         { style = "-fx-font-weight: 700; -fx-text-fill:#4E342E;" }
      )
    }

    val pie = new PieChart {
      title = s"Nutrition • ${total.calories.formatted("%.0f")} kcal"
      data = Seq(
        PieChart.Data("Protein", total.protein),
        PieChart.Data("Carbs",   total.carbs),
        PieChart.Data("Fat",     total.fat)
      )
      legendVisible = true
      labelsVisible = true
    }

    drawerPane.children = Seq(
      headerLbl,
      ingredientsLabel, ingredientList,
      sep1,
      stepsLabel, stepsBox,
      sep2,
      pie,
      macroRow
    )
  }
}
