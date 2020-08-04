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

object FutureApply {
  def main(args: Array[String]): Unit = {
      // applyしただけだと実行されない、要ec、別スレッドでの実行
      Future {
          println("start1...")
          println(s"[Thread name] in future: ${Thread.currentThread().getName}")
          Thread.sleep(1000)
          println("finish1...")
          println(s"[Thread name] in future: ${Thread.currentThread().getName}")
      }

      // 上記単体だと実行されないが、以下でスレッドを立てるとどちらも実行される
      Await.result({Future {
          println("start2...")
          println(s"[Thread name] in future: ${Thread.currentThread().getName}")
          Thread.sleep(1000)
          println("finish2...")
          println(s"[Thread name] in future: ${Thread.currentThread().getName}")
      }}, 100.seconds)
  }
}

object FutureFoldLeft {
  def main(args: Array[String]): Unit = {
      (1 to 10).foldLeft(Future.successful(0)) { (accFuture, i) =>
        println("hello.")
        Thread.sleep(1000)
        accFuture.flatMap { acc =>
          println(acc)
          println(s"[Thread name] in future: ${Thread.currentThread().getName}")
          println(i)
          // Future.successful(acc + i) successfulだとスレッドは計1つ
          // Future(acc + i) applyだとスレッドは計2つ
          Future(acc + i)
        }
      }
  }
}

object FutureNest {
  def main(args: Array[String]): Unit = {
    println("start...")
    val resultF = for {
      one <- Future{println(s"本流スレッドです ${Thread.currentThread().getName}"); 1}
      twoF <- Future(Future{Thread.sleep(5000); println(s"別スレッドです ${Thread.currentThread().getName}"); 2})
      three <- Future{Thread.sleep(1000); 3}
    } yield (one + three, twoF)
    val result = Await.result(resultF, Duration.Inf)
    println(s"計算結果 ${result._1}")
    resultF.map(result => println(s"resultさせたスレッドはどうなっているのでしょう ${Thread.currentThread().getState} 別スレッドは未完了のようです->${result._2}"))
    Thread.sleep(6000)
    result._2.map(_ => println(s"6秒経ちました。別スレッドは今どうなっているのでしょう ${Thread.currentThread().getState}"))
  }
}
