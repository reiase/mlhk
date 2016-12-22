package reiase.hackkit.Ruby

/**
  * Created by reiase on 10/15/16.
  */
class Main {
  def main(args: Array[String]): Unit = {
    val source = args.head
    val arguments = args.tail

    RubyConfig.setInit(source, arguments)

    RubyContext.main(this)
  }
}

class MainWithSpark extends Main with SparkSupport