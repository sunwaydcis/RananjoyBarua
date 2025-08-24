package view

import scalafx.scene.layout._
import scalafx.scene.Node
import scalafx.geometry.Insets
import scalafx.Includes._

import model.User
import service._

final class MainDashboardView(
  user: User,
  foodDB: FoodDatabase,
  recipeDB: RecipeDatabase,
  journalManager: JournalManager,
  mealManager: MealManager,
  onLogout: () => Unit
) extends BorderPane {

  padding = Insets(12)

  private def makeTab(id: String): Node = id match {
    case "dashboard" =>
      new DashboardTabView(
        user         = user,
        foodDB       = foodDB,
        recipeDB     = recipeDB,
        journalManager = journalManager,
        mealManager  = mealManager,
        onLogout     = onLogout,
      )

    case "food" =>
      new FoodExplorerView(foodDB.getAll)

    case "recipes" =>
      new RecipeTabView(recipeDB)

    case "compare" =>
      new CompareFoodsView(foodDB)

    case "journal" =>
      new JournalTabView(
        user          = user,
        journalManager = journalManager,
        mealManager   = mealManager,
        recipeDB      = recipeDB
      )

    case "nutrients" =>
      new NutrientRankingTabView(foodDB)

    case _ =>
      new DashboardTabView(
        user         = user,
        foodDB       = foodDB,
        recipeDB     = recipeDB,
        journalManager = journalManager,
        mealManager  = mealManager,
        onLogout     = onLogout
      )
  }

  private var currentTab: String = "dashboard"
  center = makeTab(currentTab)
  bottom = new BottomNavBar(switchTo, currentTab)

  private def switchTo(tabId: String): Unit = {
    currentTab = tabId
    center = makeTab(tabId)
    bottom = new BottomNavBar(switchTo, currentTab)
  }
}
