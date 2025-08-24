name := "NutriSmart"
version := "1.0.0"
scalaVersion := "3.3.3"

libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32"

// JavaFX modules for your OS (needed by ScalaFX)
lazy val osName = System.getProperty("os.name").toLowerCase
lazy val platform =
  if (osName.contains("win")) "win"
  else if (osName.contains("mac") && System.getProperty("os.arch") == "aarch64") "mac-aarch64"
  else if (osName.contains("mac")) "mac"
  else "linux"

libraryDependencies ++= Seq("base", "graphics", "controls", "fxml").map { m =>
  "org.openjfx" % s"javafx-$m" % "21.0.4" classifier platform
}

Compile / mainClass := Some("app.MainApp")

// Make resources (css, fonts, images) available on classpath
Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources"

// Run in a separate JVM (JavaFX apps prefer this)
ThisBuild / fork := true

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")
