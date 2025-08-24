package model

final case class User(
  name: String,
  email: String,
  passwordHash: String,
  targetCalories: Int
)
