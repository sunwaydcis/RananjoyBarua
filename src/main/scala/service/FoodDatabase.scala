package service

import scala.collection.mutable.ListBuffer
import model.{Food, NutrientProfile}

final class FoodDatabase(preloadDefaults: Boolean = true) {
  private val foodsBuf = ListBuffer.empty[Food]

  if (preloadDefaults) addFoods(FoodDatabase.defaultFoods)

  def addFoods(fs: Seq[Food]): Unit =
    foodsBuf ++= fs.map(FoodDatabase.normalizeImagePath)

  def getAll: Seq[Food] = foodsBuf.toList

  def getByName(name: String): Option[Food] =
    foodsBuf.find(_.name.equalsIgnoreCase(name))

  def searchByName(q: String): Seq[Food] = {
    val s = q.trim.toLowerCase
    foodsBuf.filter(_.name.toLowerCase.contains(s)).toList
  }

  /** rank foods descending by a nutrient name (e.g. "protein", "iron") */
  def rankFoodsBy(nutrient: String): Seq[(Food, Double)] =
    foodsBuf.view
      .map(f => (f, f.nutrients.getValueByName(nutrient)))
      .toList
      .sortBy(-_._2)
}

object FoodDatabase {
  /** Ensure imagePath starts with a leading '/' so getResource works. */
  private def normalizeImagePath(f: Food): Food = {
    val p = f.imagePath
    if (p != null && p.nonEmpty && !p.startsWith("/"))
      f.copy(imagePath = s"/images/foods/$p")
    else f
  }

  /** 10 foods with macro + micro nutrients per 100g */
  def defaultFoods: Seq[Food] = Seq(
    Food("Egg (boiled)",       NutrientProfile(155,13,1.1,11.0, 520,0.0, 1.8,50, 126,12,124, 0), "/images/foods/egg.png"),
    Food("Whole Wheat Bread",  NutrientProfile(247,13,41,4.2,   0,0.3, 3.6,107, 230,82,430, 6.7), "/images/foods/bread.png"),
    Food("Avocado",            NutrientProfile(160,2,8.5,14.7, 146,10.0,0.6,12, 485,29,7, 6.7),   "/images/foods/avocado.png"),
    Food("Chicken Breast",     NutrientProfile(165,31,0,3.6,   13,0.0, 1.0,15, 256,29,74, 0),     "/images/foods/chicken.png"),
    Food("Brown Rice",         NutrientProfile(112,2.3,23,0.8,  0,0.0, 0.4,10,  43,39,5, 1.8),    "/images/foods/rice.png"),
    Food("Spinach",            NutrientProfile(23,2.9,3.6,0.4, 9377,28.0,2.7,99, 558,79,79, 2.2), "/images/foods/spinach.png"),
    Food("Tomato",             NutrientProfile(18,0.9,3.9,0.2,  833,13.7,0.5,10, 237,11,5, 1.2),  "/images/foods/tomato.png"),
    Food("Cheddar Cheese",     NutrientProfile(402,25,1.3,33,  265,0.0, 0.7,721, 76,28,621, 0),   "/images/foods/cheese.png"),
    Food("Apple",              NutrientProfile(52,0.3,14,0.2,   54,4.6, 0.1,6,  107,5,1, 2.4),     "/images/foods/apple.png"),
    Food("Olive Oil",          NutrientProfile(884,0,0,100,      0,0.0, 0.6,1,    1,0,2, 0),       "/images/foods/oliveoil.png"),
    Food("Banana",             NutrientProfile(89,1.1,23,0.3, 64,8.7,0.3,5, 358,27,1, 2.6), "/images/foods/banana.png"),
    Food("Broccoli",           NutrientProfile(34,2.8,7,0.4,  623,89.2,0.7,47, 316,21,33, 2.6), "/images/foods/broccoli.png"),
    Food("Salmon",             NutrientProfile(208,20,0,13,   50,0.0, 0.5,9, 363,27,59, 0), "/images/foods/salmon.png"),
    Food("Almonds",            NutrientProfile(579,21,22,50,   1,0.0, 3.7,264, 705,268,1, 12.5), "/images/foods/almonds.png"),
    Food("Greek Yogurt",       NutrientProfile(59,10,3.6,0.4, 27,0.5, 0.1,110, 141,11,36, 0), "/images/foods/yogurt.png"),
    Food("Carrots",            NutrientProfile(41,0.9,10,0.2, 835,5.9,0.3,33, 320,12,69, 2.8), "/images/foods/carrot.png"),
    Food("Potato",             NutrientProfile(77,2.0,17,0.1, 2,19.7,0.8,11, 429,23,6, 2.2), "/images/foods/potato.png"),
    Food("Beef Steak",         NutrientProfile(271,25,0,19,   0,0.0, 2.6,18, 370,20,72, 0), "/images/foods/beef.png"),
    Food("Orange",             NutrientProfile(47,0.9,12,0.1, 225,53.2,0.1,40, 181,10,0, 2.4), "/images/foods/orange.png"),
    Food("Milk (whole)",       NutrientProfile(61,3.2,5,3.3,  68,0.0,0.0,113, 132,10,43, 0), "/images/foods/milk.png"),
    Food("Lentils (cooked)",   NutrientProfile(116,9.0,20,0.4, 8,1.5, 3.3,19, 369,36,2, 7.9), "/images/foods/lentils.png"),
    Food("Strawberries",       NutrientProfile(33,0.7,8,0.3, 12,59.0,0.4,16, 153,13,1, 2.0), "/images/foods/strawberry.png"),
    Food("Tofu",               NutrientProfile(76,8.0,1.9,4.8, 25,0.0, 2.7,350, 121,37,7, 0.3), "/images/foods/tofu.png"),
    Food("Peanuts",            NutrientProfile(567,25,16,49,   0,0.0, 4.6,92, 705,168,18, 8.5), "/images/foods/peanuts.png"),
    Food("Cucumber",           NutrientProfile(16,0.7,3.6,0.1, 105,2.8,0.3,16, 147,13,2, 0.5), "/images/foods/cucumber.png")
  )
}
