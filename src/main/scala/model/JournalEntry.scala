package model

import java.time.LocalDate

final case class JournalEntry(
  date: LocalDate,
  breakfast: Option[Meal] = None,
  lunch: Option[Meal]     = None,
  dinner: Option[Meal]    = None,
  notes: String           = ""
)
