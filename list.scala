object List {
  def main(args: Array[String]): Unit = {
    // 降順にする
    val optionList = Seq(Some(1), Some(3), None, Some(2))
    println(optionList.sorted.reverse)
    println(optionList.sortBy(x => x).reverse)
    println(optionList.sortBy(x => x.getOrElse(0)).reverse)
    println(
      optionList.sortWith(
        (x, y) =>
          (x, y) match {
            case (Some(xx), Some(yy)) => xx > yy
            case (Some(xx), None)     => true
            case _                    => false
        }
      )
    )
  }
}
