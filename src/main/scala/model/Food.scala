// model/Food.scala
package model

final case class Food(
  name: String,
  nutrients: NutrientProfile,
  imagePath: String            // <- must be named imagePath to match your DB
)
