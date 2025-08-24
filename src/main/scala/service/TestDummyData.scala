package service

object TestDummyData {
  val TestEmail    = "test"
  val TestPassword = "test"

  /** Ensure the demo user exists. Call once from MainApp before showing Login. */
  def ensureDemo(auth: AuthService, targetCalories: Int = 2200): Unit = {
    // If the user already exists, do nothing; else create it.
    if (Persistence.findUser(TestEmail).isEmpty) {
      auth.signup(
        name            = "Test User",
        email           = TestEmail,
        password        = TestPassword,
        targetCalories  = targetCalories
      )
    }
  }
}
