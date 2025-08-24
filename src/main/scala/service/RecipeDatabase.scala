package service

import model.{Recipe, Food, NutrientProfile}
import scala.collection.mutable.ListBuffer

final class RecipeDatabase(preloadDefaults: Boolean = true, foodDB: FoodDatabase) {
  private val buf = ListBuffer.empty[Recipe]

  if (preloadDefaults) buf ++= RecipeDatabase.defaultRecipes(foodDB)

  def addRecipe(r: Recipe): Unit = buf += r
  def addRecipes(rs: Seq[Recipe]): Unit = buf ++= rs
  def getAll: Seq[Recipe] = buf.toList
  def findByName(name: String): Option[Recipe] = buf.find(_.name.equalsIgnoreCase(name))
  def searchByIngredient(keyword: String): Seq[Recipe] = {
    val k = keyword.trim.toLowerCase
    buf.filter(_.ingredients.exists { case (f, _) => f.name.toLowerCase.contains(k) }).toList
  }
}

object RecipeDatabase {
  def defaultRecipes(foodDB: FoodDatabase): Seq[Recipe] = {
    val foods = foodDB.getAll.map(f => f.name -> f).toMap

    Seq(
      // 1
      Recipe(
        "Avocado Egg Toast",
        Seq(foods("Egg (boiled)") -> 50.0, foods("Whole Wheat Bread") -> 30.0, foods("Avocado") -> 70.0),
        Seq("Toast bread.", "Mash avocado.", "Slice egg and layer.", "Season to taste."),
        "/images/recipes/avocado_egg_toast.jpeg"
      ),

      // 2
      Recipe(
        "Chicken & Rice Bowl",
        Seq(foods("Chicken Breast") -> 120.0, foods("Brown Rice") -> 150.0, foods("Spinach") -> 50.0, foods("Tomato") -> 40.0),
        Seq("Cook rice.", "Grill chicken.", "Slice tomato and spinach.", "Assemble in bowl."),
        "/images/recipes/chicken_rice_bowl.jpeg"
      ),

      // 3
      Recipe(
        "Spinach & Apple Salad",
        Seq(foods("Spinach") -> 80.0, foods("Apple") -> 120.0, foods("Cheddar Cheese") -> 25.0, foods("Olive Oil") -> 10.0),
        Seq("Slice apple.", "Shave cheddar.", "Toss spinach with oil.", "Combine all."),
        "/images/recipes/spinach_apple_salad.jpeg"
      ),

      // 4
      Recipe(
        "Tomato Cheddar Toast",
        Seq(foods("Whole Wheat Bread") -> 60.0, foods("Tomato") -> 80.0, foods("Cheddar Cheese") -> 40.0, foods("Olive Oil") -> 5.0),
        Seq("Toast bread.", "Layer tomato & cheese.", "Melt under grill."),
        "/images/recipes/tomato_cheese_toast.jpeg"
      ),

      // 5
      Recipe(
        "Avocado Chicken Salad",
        Seq(foods("Chicken Breast") -> 100.0, foods("Avocado") -> 80.0, foods("Tomato") -> 60.0, foods("Spinach") -> 60.0),
        Seq("Dice chicken & tomato.", "Mash avocado.", "Mix with spinach."),
        "/images/recipes/avocado_chicken_salad.jpeg"
      ),

      // 6
      Recipe(
        "Apple Cheddar Sandwich",
        Seq(foods("Whole Wheat Bread") -> 70.0, foods("Apple") -> 90.0, foods("Cheddar Cheese") -> 45.0, foods("Olive Oil") -> 5.0),
        Seq("Slice apple & cheddar.", "Assemble sandwich.", "Toast until cheese melts."),
        "/images/recipes/apple_cheddar_sandwich.jpeg"
      ),

      // 7
      Recipe(
        "Mediterranean Rice Bowl",
        Seq(foods("Brown Rice") -> 160.0, foods("Tomato") -> 80.0, foods("Spinach") -> 60.0, foods("Olive Oil") -> 10.0),
        Seq("Cook rice.", "Sauté spinach.", "Top with tomato & oil."),
        "/images/recipes/mediterranean_rice_bowl.jpeg"
      ),

      // 8
      Recipe(
        "Quick Egg Fried Rice",
        Seq(foods("Brown Rice") -> 180.0, foods("Egg (boiled)") -> 100.0, foods("Spinach") -> 40.0, foods("Olive Oil") -> 8.0),
        Seq("Heat oil & fry rice.", "Add egg and spinach.", "Season and serve."),
        "/images/recipes/egg_fried_rice.jpeg"
      ),

      // 9
      Recipe(
        "Cheesy Spinach Omelette",
        Seq(foods("Egg (boiled)") -> 120.0, foods("Cheddar Cheese") -> 35.0, foods("Spinach") -> 50.0),
        Seq("Whisk eggs.", "Cook in pan.", "Add spinach & cheese, fold."),
        "/images/recipes/spinach_cheese_omelette.jpeg"
      ),

      // 10
      Recipe(
        "Avocado Tomato Salad",
        Seq(foods("Avocado") -> 100.0, foods("Tomato") -> 80.0, foods("Olive Oil") -> 10.0),
        Seq("Dice avocado & tomato.", "Toss with olive oil.", "Serve fresh."),
        "/images/recipes/avocado_tomato_salad.jpeg"
      ),

      // 11
      Recipe(
        "Apple Spinach Smoothie",
        Seq(foods("Apple") -> 150.0, foods("Spinach") -> 60.0, foods("Olive Oil") -> 5.0),
        Seq("Blend apple & spinach.", "Add olive oil.", "Serve chilled."),
        "/images/recipes/apple_spinach_smoothie.jpeg"
      ),

      // 12
      Recipe(
        "Protein Power Bowl",
        Seq(foods("Chicken Breast") -> 120.0, foods("Brown Rice") -> 100.0, foods("Egg (boiled)") -> 60.0, foods("Spinach") -> 50.0),
        Seq("Cook rice.", "Slice chicken & egg.", "Assemble with spinach."),
        "/images/recipes/protein_power_bowl.jpeg"
      ),

      // 13
      Recipe(
        "Cheddar Rice Bake",
        Seq(foods("Brown Rice") -> 200.0, foods("Cheddar Cheese") -> 50.0, foods("Spinach") -> 40.0),
        Seq("Mix cooked rice & spinach.", "Top with cheddar.", "Bake until golden."),
        "/images/recipes/cheddar_rice_bake.jpeg"
      ),

      // 14
      Recipe(
        "Avocado Apple Salad",
        Seq(foods("Avocado") -> 80.0, foods("Apple") -> 120.0, foods("Spinach") -> 50.0),
        Seq("Cube avocado & apple.", "Toss with spinach & dressing."),
        "/images/recipes/avocado_apple_salad.jpeg"
      ),

      // 15
      Recipe(
        "Tomato Rice Soup",
        Seq(foods("Brown Rice") -> 100.0, foods("Tomato") -> 100.0, foods("Spinach") -> 40.0),
        Seq("Cook tomato into broth.", "Add rice & spinach.", "Simmer until warm."),
        "/images/recipes/tomato_rice_soup.jpeg"
      ),

      // 16
      Recipe(
        "Chicken Spinach Wrap",
        Seq(foods("Chicken Breast") -> 100.0, foods("Spinach") -> 50.0, foods("Whole Wheat Bread") -> 60.0),
        Seq("Grill chicken.", "Stuff bread with chicken & spinach."),
        "/images/recipes/chicken_spinach_wrap.jpeg"
      ),

      // 17
      Recipe(
        "Egg & Cheese Sandwich",
        Seq(foods("Whole Wheat Bread") -> 70.0, foods("Egg (boiled)") -> 60.0, foods("Cheddar Cheese") -> 30.0),
        Seq("Slice egg & cheddar.", "Assemble sandwich.", "Toast until golden."),
        "/images/recipes/egg_cheese_sandwich.jpeg"
      ),

      // 18
      Recipe(
        "Apple Rice Pudding",
        Seq(foods("Apple") -> 100.0, foods("Brown Rice") -> 120.0, foods("Cheddar Cheese") -> 20.0),
        Seq("Cook rice until soft.", "Fold in grated apple.", "Top with cheese."),
        "/images/recipes/apple_rice_pudding.jpeg"
      ),

      // 19
      Recipe(
        "Avocado Spinach Smoothie",
        Seq(foods("Avocado") -> 100.0, foods("Spinach") -> 60.0, foods("Apple") -> 80.0),
        Seq("Blend avocado, spinach, apple.", "Serve chilled."),
        "/images/recipes/avocado_spinach_smoothie.jpeg"
      ),

      // 20
      Recipe(
        "Cheesy Chicken Rice",
        Seq(foods("Chicken Breast") -> 120.0, foods("Brown Rice") -> 150.0, foods("Cheddar Cheese") -> 40.0),
        Seq("Cook rice.", "Stir in chicken.", "Top with cheddar."),
        "/images/recipes/cheesy_chicken_rice.jpeg"
      ),

      // 21
      Recipe(
        "Mediterranean Veggie Wrap",
        Seq(foods("Whole Wheat Bread") -> 60.0, foods("Spinach") -> 40.0, foods("Tomato") -> 50.0, foods("Cheddar Cheese") -> 25.0, foods("Olive Oil") -> 10.0),
        Seq("Warm the bread slightly to make it pliable.", "Drizzle olive oil over the wrap.", "Layer spinach, sliced tomato, and cheddar inside.", "Roll up tightly, slice in half, and serve."),
        "/images/recipes/mediterranean_veggie_wrap.jpeg"
      )
    )  
  }
}
