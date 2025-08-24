package view

import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._

import service.AuthService
import model.User

final class LoginView(
  authService: AuthService,
  onLoginSuccess: User => Unit,
  onSwitchToSignUp: () => Unit
) extends VBox {

  spacing = 12
  padding = Insets(20)
  alignment = Pos.Center

  private val emailField = new TextField { promptText = "Email" ; prefWidth = 280 }
  private val passField  = new PasswordField { promptText = "Password"; prefWidth = 280 }

  private val status = new Label("") { style = "-fx-text-fill: #B00020;" }

  private val loginBtn = new Button("Login") {
    defaultButton = true
    onAction = _ => doLogin()
  }

  private val demoBtn = new Button("Login as Demo") {
    onAction = _ => {
      // Create demo user if not exist
      val name  = "Demo"
      val email = "demo@nutri.app"
      val pass  = "demo123"
      val target= 2200
      authService.signup(name, email, pass, target) // ignore Left if exists
      authService.login(email, pass).foreach(onLoginSuccess)
    }
  }

  private val signupLink = new Hyperlink("Sign up") {
    onAction = _ => onSwitchToSignUp()
  }

  // ---- Logo + Title aligned left ----
  private val logoAndTitle = new HBox {
    spacing = 8
    alignment = Pos.Center
    children = Seq(
      new ImageView(new Image(getClass.getResourceAsStream("/images/logo.png"))) {
        fitHeight = 35
        fitWidth = 35
        preserveRatio = true
      },
      new Label("NutriSmart") { styleClass += "label-title" }
    )
  }

  children = Seq(
    logoAndTitle,
    emailField,
    passField,
    new HBox { spacing = 10; alignment = Pos.CenterLeft; children = Seq(loginBtn, demoBtn, new Label("or"), signupLink) },
    status
  )

  private def doLogin(): Unit = {
    val e = emailField.text.value.trim
    val p = passField.text.value
    authService.login(e, p) match {
      case Some(u) => onLoginSuccess(u)
      case None    => status.text = "Invalid email or password."
    }
  }
}
