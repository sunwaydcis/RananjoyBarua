package service

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import scala.util.Try

import model.User

object Persistence {
  private val BaseDir: Path = {
    val home = sys.props.getOrElse("user.home", ".")
    Paths.get(home, ".nutrismart")
  }

  // ---------- FS helpers ----------
  def ensureDir(p: Path): Unit = if (!Files.exists(p)) Files.createDirectories(p)
  def userDir(email: String): Path = {
    val safe = email.toLowerCase.replaceAll("[^a-z0-9._-]", "_")
    val p = BaseDir.resolve("users").resolve(safe)
    ensureDir(p); p
  }

  private def write(p: Path, bytes: Array[Byte]): Unit = {
    ensureDir(p.getParent)
    Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
  }
  private def append(p: Path, s: String): Unit = {
    ensureDir(p.getParent)
    Files.write(p, s.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }
  private def readOpt(p: Path): Option[String] =
    if (Files.exists(p)) Some(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)) else None

  def writeText(p: Path, s: String): Unit = write(p, s.getBytes(StandardCharsets.UTF_8))
  def appendText(p: Path, s: String): Unit = append(p, s)
  def readTextOpt(p: Path): Option[String] = readOpt(p)

  // ---------- Users ----------
  private def usersDir: Path = { val p = BaseDir.resolve("users"); ensureDir(p); p }

  private def userMeta(email: String): Path = userDir(email).resolve("user.txt")

  def saveUser(u: User): Unit = {
    val lines =
      s"""name=${u.name}
email=${u.email}
hash=${u.passwordHash}
targetCalories=${u.targetCalories}
"""
    writeText(userMeta(u.email), lines)
  }

  def findUser(email: String): Option[User] = {
    readTextOpt(userMeta(email)).flatMap { txt =>
      val kv = txt.linesIterator.flatMap { ln =>
        ln.split("=", 2) match {
          case Array(k, v) => Some(k.trim -> v.trim)
          case _           => None
        }
      }.toMap
      for {
        name   <- kv.get("name")
        mail   <- kv.get("email")
        hash   <- kv.get("hash")
        target <- kv.get("targetCalories").flatMap(s => Try(s.toInt).toOption)
      } yield User(name, mail, hash, target)
    }
  }

  // ---------- Hashing ----------
  def sha256(s: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(s.getBytes(StandardCharsets.UTF_8))
    bytes.map("%02x".format(_)).mkString
  }
}
