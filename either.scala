sealed trait LoginError

case object InvalidPassword extends LoginError
case object InvalidNotFound extends LoginError

case class User(id: Long, name: String, password: String)

object LoginService {
  val user1 = User(1L, "natsumi", "0000")
  // Eitherは二つの型をもつ可能性があることを示す型
  // Either自身は抽象クラスで、サブタイプとしてLeft型とRight肩をもつ
  // Either[LoginError, User]はLeft[LoginError, User]かもしれないしRigth[LoginError, User]かもしれない
  def login(name: String, password: String): Either[LoginError, User] = {
    if (user1.name == name) {
      if (user1.password == password) {
        Right(user1)
      } else {
        Left(InvalidPassword)
      }
    } else {
      Left(InvalidNotFound)
    }
  }
}

object LoginController {
  def main(args: Array[String]): Unit = {
    LoginService.login("natsumi", "0000") match {
      case Right(user) => println(s"id: ${user.id}")
      case Left(InvalidPassword) => println(s"Invalid Password!")
      case Left(InvalidNotFound) => println(s"Invalid Username!")
    }
  }
}