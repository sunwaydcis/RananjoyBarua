package view

import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._

import service.AuthService
import model.User

final class SignUpView(
  authService: AuthService,
  onSignedUp: User => Unit,
  onBackToLogin: () => Unit
) extends VBox {

  spacing = 12
  padding = Insets(20)
  alignment = Pos.Center

  private val nameField   = new TextField       { promptText = "Full name";       prefWidth = 300 }
  private val emailField  = new TextField       { promptText = "Email";           prefWidth = 300 }
  private val passField   = new PasswordField   { promptText = "Password";        prefWidth = 300 }
  private val calsField   = new TextField       { promptText = "Target calories"; prefWidth = 300 }

  private val status = new Label("") { style = "-fx-text-fill: #B00020;" }

  private val signupBtn = new Button("Create Account") {
    defaultButton = true
    onAction = _ => doSignup()
  }
  private val backLink = new Hyperlink("Back to login") {
    onAction = _ => onBackToLogin()
  }

  // --- Logo + Title side by side ---
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
    nameField, emailField, passField, calsField,
    new HBox { spacing = 10; alignment = Pos.CenterLeft; children = Seq(signupBtn, backLink) },
    status
  )

  private def doSignup(): Unit = {
    val name = nameField.text.value.trim
    val mail = emailField.text.value.trim
    val pass = passField.text.value
    val cals = calsField.text.value.trim.toIntOption.getOrElse(2000)

    if (name.isEmpty || mail.isEmpty || pass.isEmpty) {
      status.text = "Please fill in all fields."; return
    }
    authService.signup(name, mail, pass, cals) match {
      case Left(err) => status.text = err
      case Right(u)  => onSignedUp(u)
    }
  }
}
