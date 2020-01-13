import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}
import java.util.{Date, Locale}
import java.text.DateFormat._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

object FutureSample {
  val text = "Hello!"
  /*
  Futureシングルトンは関数を与えるとその関数を非同期に与えるFuture[+T]を返す
    */
  val f: Future[String] = Future {
    Thread.sleep(1000)
    println(s"[Thread name] in future: ${Thread.currentThread().getName}")
    // 別スレッド
    s"$text future!"
  } // 5

  def main(args: Array[String]): Unit = {
    // onSuccessは非推奨ではある
    f.onSuccess {
      case text: String =>
        println(s"[Thread name] in future: ${Thread.currentThread().getName}")
        // 別スレッド
        println(text)
    } // 2
    println(f.isCompleted) // 1
    Await.ready(f, 5000 millisecond)
    println(s"[Thread name] in future: ${Thread.currentThread().getName}") // 3
    // mainスレッド
    println(f.isCompleted) // 4
  }
}

object FutureOptionUsageSample {
  val random = new Random()
  val waitMaxMillSec = 3000

  def main(args: Array[String]): Unit = {
    val futureMilliSec: Future[Int] = Future {
      val waitMilliSec = random.nextInt(waitMaxMillSec)
      if (waitMilliSec < 1000) throw new RuntimeException(s"waitMilliSec is $waitMilliSec")
      println(s"[Thread name] in future: ${Thread.currentThread().getName} wait $waitMilliSec milli sec")
      Thread.sleep(waitMilliSec)
      waitMilliSec
    }

    // 上と同様のスレッドで動く
    val futureSec: Future[Double] = futureMilliSec.flatMap { milliSec =>
      println(s"[Thread name] in future: ${Thread.currentThread().getName}")
      Future(milliSec.toDouble / 1000)
    }

    futureSec.onComplete {
      // 成功と失敗の両方の処理を記述したい場合はonComplate
      case Success(waitSec) => println(s"Success! $waitSec sec")
      case Failure(e) => println(s"Fuilure: ${e.getMessage}")
    }

    Thread.sleep(3000)
  }
}

// Futureを複数組み合わせてみる
object CompositeFutureSample {
  val random = new Random()
  val waitMaxMilliSec = 3000

  def waitRandom(futureName: String): Int = {
    val waitMilliSec = random.nextInt(waitMaxMilliSec)
    if (waitMilliSec < 500) throw new RuntimeException(s"${futureName} waitMilliSec is ${waitMilliSec}")
    println(s"[Thread name] in future: ${Thread.currentThread().getName}")
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }

  def main(args: Array[String]): Unit = {
    val futureFirst: Future[Int] = Future(waitRandom("first"))
    val futureSecond: Future[Int] = Future(waitRandom("second"))

    val compositeFuture: Future[(Int, Int)] = for {
      first: Int <- futureFirst
      second: Int <- futureSecond
    } yield (first, second)

    // for式使わない場合は
    // futureFirst.flatMap(f => futureSecond.map(s => (f, s)))

    compositeFuture.onComplete {
      case Success((f, s)) => {
        println(s"[Thread name] in future: ${Thread.currentThread().getName}")
        println(s"Success: $f, $s")
      }
      case Failure(e) => println(s"Failure: ${e.getMessage}")
    }

    Await.ready(compositeFuture, 5000 millisecond)
  }
}