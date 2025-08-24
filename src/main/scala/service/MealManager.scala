package service

import java.nio.file.{Files, Path, StandardOpenOption}
import java.time.LocalDate
import scala.jdk.CollectionConverters._

import model.{NutrientProfile, Recipe, MealType}
import scalafx.beans.property.LongProperty   // for UI change notifications

/** Persists per-user meals and computes daily totals. */
final class MealManager(userEmail: String, foodDB: FoodDatabase, recipeDB: RecipeDatabase) {

  // ---- storage ----
  private val baseDir: Path = Persistence.userDir(userEmail).resolve("meals")
  Persistence.ensureDir(baseDir)

  // ---- simple change signal for UI ----
  private var _version: Long = 0L
  private val _versionProp: LongProperty = LongProperty(_version)
  /** UI can listen to this: mealManager.mealsChanged.onChange { ... } */
  def mealsChanged: LongProperty = _versionProp
  private def bump(): Unit = { _version += 1; _versionProp.value = _version }

  /** Public view model for a logged meal. */
  final case class Entry(date: LocalDate, mealType: MealType, name: String, servings: Double, note: String)

  // ---------------------------
  // Write side
  // ---------------------------
  /** Add a meal to the log from a recipe. */
  def addFromRecipe(date: LocalDate, mealType: MealType, recipe: Recipe, servings: Double, note: String = ""): Unit = {
    val file = fileFor(date)
    val line = s"${mealType.toString},${escape(recipe.name)},$servings,${escape(note)}\n"
    Files.write(
      file,
      line.getBytes("UTF-8"),
      StandardOpenOption.CREATE, StandardOpenOption.APPEND
    )
    bump() // notify UI
  }

  // ---------------------------
  // Read side
  // ---------------------------
  def getMealsOn(date: LocalDate): Seq[Entry] = readDay(fileFor(date))

  def getMealsTodayNames(): Seq[String] = getMealsOn(LocalDate.now()).map(_.name)

  /** Total nutrients for a given date (summing all entries). */
  def totalNutrientsOn(date: LocalDate): NutrientProfile =
    getMealsOn(date).foldLeft(NutrientProfile.zero) { (acc, e) =>
      acc + nutrientsFor(e)
    }

  /** Today’s totals. */
  def totalNutrientsToday(): NutrientProfile = totalNutrientsOn(LocalDate.now())

  /** (date, totals) for the last n days (inclusive of today). */
  def lastNDays(n: Int): Seq[(LocalDate, NutrientProfile)] = {
    val today = LocalDate.now()
    (0 until n).map { i =>
      val d = today.minusDays((n - 1 - i).toLong)
      d -> totalNutrientsOn(d)
    }
  }

  /** Compute nutrients for a single logged entry by re‑looking up the recipe and
    * scaling each ingredient’s grams by the entry’s `servings`.
    */
  def nutrientsFor(e: Entry): NutrientProfile = {
    recipeDB.findByName(e.name).map { r =>
      r.ingredients
        .map { case (food, grams) => food.nutrients.scaleTo(grams * e.servings) }
        .foldLeft(NutrientProfile.zero)(_ + _)
    }.getOrElse(NutrientProfile.zero)
  }

  // ---------------------------
  // Files
  // ---------------------------
  private def fileFor(d: LocalDate): Path =
    baseDir.resolve(f"${d.getYear}%04d-${d.getMonthValue}%02d-${d.getDayOfMonth}%02d.csv")

  private def readDay(p: Path): Seq[Entry] = {
    if (!Files.exists(p)) return Seq.empty
    Files.readAllLines(p, java.nio.charset.StandardCharsets.UTF_8)
      .asScala
      .flatMap(parseLine(_, p.getFileName.toString))
      .toVector
  }

  /** CSV: mealType,name,servings,note  — date comes from filename yyyy-MM-dd.csv */
  private def parseLine(line: String, fileName: String): Option[Entry] = {
    val parts = splitCsv(line)
    if (parts.length < 4) return None

    val mt = try MealType.valueOf(parts(0)) catch { case _: Throwable => MealType.Lunch }
    val nm = unescape(parts(1))
    val sv = parts(2).toDoubleOption.getOrElse(1.0)
    val nt = unescape(parts(3))

    val dateStr = fileName.takeWhile(_ != '.')
    val date    = LocalDate.parse(dateStr)

    Some(Entry(date, mt, nm, sv, nt))
  }

  // ---------------------------
  // Tiny CSV helpers (escape with backslash)
  // ---------------------------
  private def splitCsv(s: String): Array[String] = {
    val out = scala.collection.mutable.ArrayBuffer.newBuilder[String]
    val sb  = new StringBuilder
    var i   = 0
    while (i < s.length) {
      val c = s.charAt(i)
      if (c == '\\' && i + 1 < s.length) { sb.append(s.charAt(i + 1)); i += 2 }
      else if (c == ',') { out += sb.toString(); sb.clear(); i += 1 }
      else { sb.append(c); i += 1 }
    }
    out += sb.toString()
    out.result().toArray
  }
  private def escape(s: String): String = s.flatMap {
    case '\\' => "\\\\"
    case ','  => "\\,"
    case c    => c.toString
  }
  private def unescape(s: String): String = {
    val sb = new StringBuilder
    var i  = 0
    while (i < s.length) {
      val c = s.charAt(i)
      if (c == '\\' && i + 1 < s.length) { sb.append(s.charAt(i + 1)); i += 2 }
      else { sb.append(c); i += 1 }
    }
    sb.toString()
  }
}
