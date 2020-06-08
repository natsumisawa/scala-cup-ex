case class Animal(name: String) {
  def printName: Unit =
    // classの持つフィールドに対して何かする
    println(name)
}

object Animal {
  def apply(name: String): Animal =
    new Animal(s"Ms $name")
}

object Main {
  def main(args: Array[String]): Unit = {
      val animal = Animal("b")
      animal.printName
  }
}
