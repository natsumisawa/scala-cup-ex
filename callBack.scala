import java.time.ZonedDateTime

import scala.util.Random

object Simulation {
  def main(args: Array[String]): Unit = {
    (1 to 3).foreach { request }
  }

  private def request(i: Int): Unit = {
    println(ZonedDateTime.now())
    Thread.sleep(1000)
    println(s"----リクエストしたよ-----$i")
  }
}

object Timer {
  def oncePerSecond(callback: () => Unit) =
    while (true) {
      callback()
      Thread sleep 1000
    }
  def main(args: Array[String]): Unit = {
    oncePerSecond({() =>
      // ここはその都度評価される
      val num = Random.nextInt(10)
      println(s"time flies like an arrow...$num")
    })
  }
}