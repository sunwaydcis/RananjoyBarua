package service

import java.nio.file.{Files, Path}
import java.time.LocalDate

object JournalManager {
  private def fileFor(dir: Path, date: LocalDate): Path =
    dir.resolve(f"${date.getYear}%04d-${date.getMonthValue}%02d-${date.getDayOfMonth}%02d.txt")
}

final class JournalManager(userEmail: String) {
  import JournalManager._
  private val dir: Path = Persistence.userDir(userEmail).resolve("journal")
  Persistence.ensureDir(dir)

  def setNotes(date: LocalDate, text: String): Unit = {
    val file = fileFor(dir, date)
    Persistence.writeText(file, Option(text).getOrElse(""))
  }

  def getNotes(date: LocalDate): Option[String] =
    Persistence.readTextOpt(fileFor(dir, date))
}
