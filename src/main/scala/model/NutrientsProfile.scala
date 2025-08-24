package model

final case class NutrientProfile(
  calories: Double, protein: Double, carbs: Double, fat: Double,
  vitaminA: Double, vitaminC: Double, iron: Double, calcium: Double,
  potassium: Double, magnesium: Double, sodium: Double, fiber: Double
) {
  /** Scale linearly to a given grams (assuming values are per 100g). */
  def scaleTo(grams: Double): NutrientProfile = {
    val k = grams / 100.0
    this * k
  }

  /** Multiply all nutrients by a scalar. */
  def *(k: Double): NutrientProfile =
    NutrientProfile(
      calories * k, protein * k, carbs * k, fat * k,
      vitaminA * k, vitaminC * k, iron * k, calcium * k,
      potassium * k, magnesium * k, sodium * k, fiber * k
    )

  def +(o: NutrientProfile): NutrientProfile =
    NutrientProfile(
      calories + o.calories, protein + o.protein, carbs + o.carbs, fat + o.fat,
      vitaminA + o.vitaminA, vitaminC + o.vitaminC, iron + o.iron, calcium + o.calcium,
      potassium + o.potassium, magnesium + o.magnesium, sodium + o.sodium, fiber + o.fiber
    )

  /** Helper for dashboard charts/ranking by string key (case-insensitive). */
  def getValueByName(name: String): Double =
    name.trim.toLowerCase match {
      case "calories"  => calories
      case "protein"   => protein
      case "carbs"     => carbs
      case "fat"       => fat
      case "vitamin a" => vitaminA
      case "vitamin c" => vitaminC
      case "iron"      => iron
      case "calcium"   => calcium
      case "potassium" => potassium
      case "magnesium" => magnesium
      case "sodium"    => sodium
      case "fiber"     => fiber
      case _           => 0.0
    }
}

object NutrientProfile {
  val zero: NutrientProfile =
    NutrientProfile(0,0,0,0, 0,0,0,0, 0,0,0,0)
}
