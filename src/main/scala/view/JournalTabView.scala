package view

import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.control._
import scalafx.scene.text.Font
import scalafx.geometry.{Insets, Pos}
import scalafx.collections.ObservableBuffer
import scalafx.Includes._

import java.time.LocalDate

import model.{User, MealType, Recipe, NutrientProfile}
import service.{JournalManager, MealManager, RecipeDatabase}

final class JournalTabView(
  user: User,
  journalManager: JournalManager,
  mealManager: MealManager,
  recipeDB: RecipeDatabase
) extends VBox {

  // ---------- layout basics ----------
  spacing = 12
  padding = Insets(12)

  // ---------- Title ----------
  private val title = new Label("Journal & Food Log") {
    styleClass += "label-title"
  }

  // ---------- Row 1: Date + MealType ----------
  private val datePicker = new DatePicker(LocalDate.now())
  private val mealTypeBox = new ComboBox[MealType](ObservableBuffer(MealType.values.toSeq*)) {
    value = MealType.Lunch
    prefWidth = 140
  }

  private val row1 = new HBox {
    spacing = 8
    alignment = Pos.CenterLeft
    children = Seq(
      new Label("Date:"), datePicker,
      new Label("Meal:"), mealTypeBox
    )
  }

  // ---------- Row 2: Recipe picker + servings ----------
  private val allRecipes: Seq[Recipe] = recipeDB.getAll
  private val recipeNames = ObservableBuffer(allRecipes.map(_.name)*)

  private val recipeBox = new ComboBox[String](recipeNames) {
    promptText = "Choose a recipe…"
    prefWidth = 260
  }

  private val servingsField = new TextField {
    promptText = "Servings (e.g. 1.0)"
    text = "1.0"
    prefWidth = 110
  }

  private val row2 = new HBox {
    spacing = 8
    alignment = Pos.CenterLeft
    children = Seq(
      new Label("Recipe:"),   recipeBox,
      new Label("Servings:"), servingsField
    )
  }

  // ---------- Row 3: Add Meal button + feedback ----------
  private val addBtn = new Button("Add Meal from Recipe") {
    styleClass += "button"
    onAction = _ => addMealFromRecipe()
  }
  private val feedback = new Label("") {
    style = "-fx-text-fill: #4E342E;"
  }

  private val row3 = new HBox {
    spacing = 10
    alignment = Pos.CenterLeft
    children = Seq(addBtn, feedback)
  }

  // ---------- Meals list + totals ----------
  private val mealsList = new ListView[String] {
    prefHeight = 180
  }

  private val calsLabel = new Label("Calories today: 0 kcal") {
    style = "-fx-font-weight: 700;"
    font = Font.font("Poppins", 14)
  }

  private val macroLabel = new Label("Protein / Carbs / Fat: 0.0 / 0.0 / 0.0 g")

  // ---------- Daily notes ----------
  private val notes = new TextArea {
    promptText = "Add your notes for today..."
    prefRowCount = 6
  }

  private val saveNotesBtn = new Button("Save Notes") {
    onAction = _ => {
      val d = datePicker.value.value
      journalManager.setNotes(d, notes.text.value)
      feedback.text = s"Notes saved for $d."
    }
  }

  // ---------- Assemble ----------
  children = Seq(
    title,
    row1,
    row2,
    row3,
    new Label("Meals for date:") { styleClass += "label-section" },
    mealsList,
    new HBox { spacing = 10; children = Seq(calsLabel, macroLabel) },
    new Separator,
    new Label("Daily Journal") { styleClass += "label-section" },
    notes,
    saveNotesBtn
  )

  // ---------- Wiring / behavior ----------
  // Refresh on date change
  datePicker.value.onChange { (_, _, _) => 
    refreshMealsPanel()
    loadNotesForSelectedDate()
  }
  // Refresh when MealManager notifies
  mealManager.mealsChanged.onChange { (_, _, _) => 
    refreshMealsPanel()
  }

  // Initial load
  refreshMealsPanel()
  loadNotesForSelectedDate()

  // ---------- Helpers ----------

  private def loadNotesForSelectedDate(): Unit = {
    val d = datePicker.value.value
    notes.text = journalManager.getNotes(d).getOrElse("")
  }

  private def addMealFromRecipe(): Unit = {
    val d  = datePicker.value.value
    val mt = mealTypeBox.value.value

    val rOpt: Option[Recipe] =
      Option(recipeBox.value.value).flatMap(name => allRecipes.find(_.name == name))

    rOpt match {
      case None =>
        feedback.text = "Choose a recipe first."
      case Some(recipe) =>
        parsePositiveDouble(servingsField.text.value) match {
          case None =>
            feedback.text = "Enter a valid servings number (e.g. 1.0)."
          case Some(servings) if servings <= 0.0 =>
            feedback.text = "Servings must be greater than 0."
          case Some(servings) =>
            mealManager.addFromRecipe(date = d, mealType = mt, recipe = recipe, servings = servings, note = "")
            feedback.text = s"Added ${recipe.name} × $servings to $d."
            refreshMealsPanel()
        }
    }
  }

  private def refreshMealsPanel(): Unit = {
    val d = datePicker.value.value
    val entries = mealManager.getMealsOn(d)

    // Show: "MealType — Recipe (xN) — K kcal"
    val lines = entries.map { e =>
      val kcal = f"${mealManager.nutrientsFor(e).calories}%.0f"
      s"${e.mealType.toString} — ${e.name} (x${e.servings}) — $kcal kcal"
    }
    mealsList.items = ObservableBuffer(lines*)

    val totals: NutrientProfile = mealManager.totalNutrientsOn(d)
    calsLabel.text  = f"Calories on $d: ${totals.calories}%.0f kcal"
    macroLabel.text = f"Protein / Carbs / Fat: ${totals.protein}%.1f / ${totals.carbs}%.1f / ${totals.fat}%.1f g"
  }

  private def parsePositiveDouble(s: String): Option[Double] =
    scala.util.Try(s.trim.toDouble).toOption.filter(_ > 0)
}
