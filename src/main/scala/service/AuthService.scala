package service

import model.User

final class AuthService {

  /** Return Some(user) if email/password are correct, else None */
  def login(email: String, password: String): Option[User] = {
    val hash = Persistence.sha256(password)
    Persistence.findUser(email).filter(_.passwordHash == hash)
  }

  /** Create a user if email not taken. Returns Right(user) on success, Left(error) otherwise. */
  def signup(name: String, email: String, password: String, targetCalories: Int): Either[String, User] = {
    if (Persistence.findUser(email).isDefined) Left("Email already registered.")
    else {
      val u = User(name = name.trim, email = email.trim.toLowerCase, passwordHash = Persistence.sha256(password), targetCalories = targetCalories)
      Persistence.saveUser(u)
      Right(u)
    }
  }
}
