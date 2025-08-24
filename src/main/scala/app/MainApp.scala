package app

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import scalafx.Includes._

import model.User
import service._
import view._

object MainApp extends JFXApp3 {

  // Shared DBs
  val foodDB   = new FoodDatabase(preloadDefaults = true)
  val recipeDB = new RecipeDatabase(preloadDefaults = true, foodDB = foodDB)
  val auth     = new AuthService

  // root container we swap
  private val rootPane = new BorderPane

  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "NutriSmart"
      scene = new Scene(1200, 800) {
        root = rootPane
        val css = getClass.getResource("/styles/app.css")
        if (css != null) stylesheets += css.toExternalForm
      }
    }
    // Start on login
    showLogin()
  }

  private def showLogin(): Unit = {
    val view = new LoginView(
      authService      = auth,
      onLoginSuccess   = user => openDashboard(user),
      onSwitchToSignUp = () => showSignup()
    )
    rootPane.center = view
  }

  private def showSignup(): Unit = {
    val view = new SignUpView(
      authService   = auth,
      onSignedUp    = user => openDashboard(user),
      onBackToLogin = () => showLogin()   // <-- fixed name
    )
    rootPane.center = view
  }

  private def openDashboard(user: User): Unit = {
    val mealMgr    = new MealManager(user.email, foodDB, recipeDB)
    val journalMgr = new JournalManager(user.email)

    val dash = new MainDashboardView(
      user           = user,
      foodDB         = foodDB,
      recipeDB       = recipeDB,
      journalManager = journalMgr,
      mealManager    = mealMgr,
      onLogout       = () => showLogin()
    )
    rootPane.center = dash
  }
}
