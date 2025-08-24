package view

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.chart._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Font
import scalafx.Includes._

import java.time.LocalDate

import model.User
import service.{JournalManager, MealManager, RecipeDatabase, FoodDatabase}
import model.NutrientProfile

final class DashboardTabView(
    user: User,
    foodDB: FoodDatabase,
    recipeDB: RecipeDatabase,
    journalManager: JournalManager,
    mealManager: MealManager,
    onLogout: () => Unit
) extends BorderPane {

  padding = Insets(10)

  // ---- HEADER (top-left) ----
  private val header = new Label(s"Welcome, ${user.name}") {
    styleClass += "label-title"
    font = Font.font("Poppins", 18)
  }

  // ---- ORANGE LOGOUT BUTTON (top-right) ----
  private val logoutBtn = new Button("Logout") {
    style =
      "-fx-background-color: #E65100; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;"
    onAction = _ => onLogout()
  }

  // ---- TOP BAR (header + spacer + logout button) ----
  private val topBar = new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(5, 10, 5, 10)
    spacing = 10
    children = Seq(
      header,
      new Region { HBox.setHgrow(this, Priority.Always) }, // spacer pushes button to right
      logoutBtn
    )
  }
  top = topBar

  // ---- CALORIES SUMMARY ----
  private val today = LocalDate.now()
  private val totals: NutrientProfile = mealManager.totalNutrientsOn(today)

  private val calsLabel = new Label(f"Calories today: ${totals.calories}%.0f kcal") {
    font = Font.font("Poppins", 16)
    style = "-fx-font-weight: 700;"
  }
  private val macrosLabel = new Label(
    f"Protein: ${totals.protein}%.1f g  |  Carbs: ${totals.carbs}%.1f g  |  Fat: ${totals.fat}%.1f g"
  ) { font = Font.font("Poppins", 13) }

  private val summaryBox = new VBox {
    spacing = 6
    children = Seq(calsLabel, macrosLabel)
  }

  // ---- MEALS TODAY (names list) ----
  private val foodsToday = ObservableBuffer(
    mealManager.getMealsOn(today).map(_.name)*
  )
  private val foodsList = new ListView[String](foodsToday) {
    prefHeight = 150
  }

  // ---- MACROS BAR CHART (7 days) ----
  private val xAxis = new CategoryAxis()
  private val yAxis = new NumberAxis()
  private val barChart = new BarChart[String, Number](xAxis, yAxis) {
    title = "Last 7 Days • Protein / Carbs / Fat"
    categoryGap = 12
    barGap = 4
    legendVisible = true
  }

  private def last7Days(): Seq[(LocalDate, NutrientProfile)] =
    (0 until 7).reverse.map { off =>
      val d = today.minusDays(off)
      d -> mealManager.totalNutrientsOn(d)
    }

  private def refreshBarChart(): Unit = {
    val days = last7Days()

    val proteinSeries = new XYChart.Series[String, Number] {
      name = "Protein"
      data = ObservableBuffer(days.map { case (d, n) =>
        XYChart.Data[String, Number](d.toString, n.protein)
      }*)
    }
    val carbsSeries = new XYChart.Series[String, Number] {
      name = "Carbs"
      data = ObservableBuffer(days.map { case (d, n) =>
        XYChart.Data[String, Number](d.toString, n.carbs)
      }*)
    }
    val fatSeries = new XYChart.Series[String, Number] {
      name = "Fat"
      data = ObservableBuffer(days.map { case (d, n) =>
        XYChart.Data[String, Number](d.toString, n.fat)
      }*)
    }

    // Use delegates to satisfy JavaFX type for chart series
    barChart.data = Seq(proteinSeries.delegate, carbsSeries.delegate, fatSeries.delegate)
  }
  refreshBarChart()

  // ---- Layout ----
  center = new VBox {
    spacing = 12
    padding = Insets(10)
    children = Seq(
      summaryBox,
      new Label("Meals Today") { styleClass += "label-section" },
      foodsList,
      barChart
    )
  }
}
