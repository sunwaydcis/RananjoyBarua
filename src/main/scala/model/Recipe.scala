package model

final case class Recipe(
  name: String,
  ingredients: Seq[(Food, Double)], // (Food, grams)
  steps: Seq[String],
  imagePath: String = ""            // e.g. "/images/recipes/avocado_egg_toast.jpeg"
)
