import java.util.{Date, Locale}
import java.text.DateFormat._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
  }
}

object FrenchData {
  def main(args: Array[String]): Unit = {
    val now = new Date
    val df = getDateInstance(1, Locale.FRANCE)
    println(df format now)
  }
}

object Timer {
  def onecePerSecond(callBack: () => Unit): Unit = {
    while (true) {
      callBack()
      Thread sleep 1000
    }
  }

  def timefiles(): Unit = {
    println("time files like an arrow...")
  }

  def main(args: Array[String]): Unit = {
    onecePerSecond(timefiles) // 評価はしない、関数オブジェクトを渡しているだけ
    onecePerSecond(() =>
      println("無名関数...一つ目のonecePerSecondをコメントアウトしないと評価されないよ")
    )
  }
}

class Complex(real: Double, imaginary: Double) {
  def re = real

  val im = imaginary

  override def toString() =
    s"$re ${if (im >= 0) "+" else ""} $im i"
}

object ComplexNumbers {
  def main(args: Array[String]): Unit = {
    val c = new Complex(1.2, 3.4)
    println(s"imaginary part: ${c.im}")
    println(s"overridden toString: ${c.toString()}")
  }
}

abstract class Tree

case class Sum(l: Tree, r: Tree) extends Tree

case class Var(n: String) extends Tree

case class Const(v: Int) extends Tree

object Tree {
  type Environment = String => Int

  def eval(t: Tree, env: Environment): Int = t match {
    case Sum(l, r) => eval(l, env) + eval(r, env)
    case Var(n) => env(n)
    case Const(v) => v
  }

  def derive(t: Tree, v: String): Tree = t match {
    case Sum(l, r) => Sum(derive(l, v), derive(r, v))
    case Var(n) if v == n => Const(1)
    case _ => Const(0)
  }

  def main(args: Array[String]): Unit = {
    val exp: Tree = Sum(Sum(Var("x"), Var("x")), Sum(Const(1), Var("y")))
    val env: Environment = {
      case "x" => 5
      case "y" => 7
    }

    println(s"expression: $exp")
    println(s"evaluation with x=5 y=7 ${eval(exp, env)}")
    println(s"derivative relative to x: ${derive(exp, "x")}")
  }
}

object ImplicitClass {

  implicit class DoubleInt(i: Int) {
    def double = i * 2
  }

  class HalfInt(i: Int) {
    def half = i / 2
  }

  implicit def toHalfInt(i: Int) = new HalfInt(i)

  def main(args: Array[String]): Unit = {
    println(1.double)
    println(1.half)
  }
}

class PreferredPrompt(val preference: String)

object JoesPrefs {
  implicit val prompt = new PreferredPrompt("yes, master... >")
}

object PreferredPrompt {
  def greet(name: String)(implicit prompt: PreferredPrompt) = {
    println(s"welcome, $name. The system is ready.")
    println(prompt.preference)
  }

  def main(args: Array[String]): Unit = {
    val p = new PreferredPrompt(":) >>>>>>>")
    greet("natusmi")(p)

    import JoesPrefs._
    greet("joe") // Joeのためのpromptが暗黙的に渡される
  }
}

