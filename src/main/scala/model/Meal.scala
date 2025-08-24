package model

import java.time.LocalDate

enum MealType { case Breakfast, Lunch, Dinner, Snack }

final case class Meal(
  name: String,
  mealType: MealType,
  nutrients: NutrientProfile,
  date: LocalDate,
  items: Seq[(Food, Double)] // (food, grams)
)
