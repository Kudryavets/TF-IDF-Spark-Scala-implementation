import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}


abstract class AbstractJob {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    logger.info(s"start with arguments: ${args.mkString(",")}")
    val sparkSession = SparkSession.builder()
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    try {
      doWork(sparkSession, args.map(removeQuotes))
    } finally {
      sparkSession.stop()
    }
  }

  def doWork(sparkSession: SparkSession, args: Array[String]): Unit

  private def removeQuotes(str: String): String = {
    if (isQuoted(str)) {
      logger.info(s"remoteQuotes() for param = $str")
      removeQuotes(str.substring(1, str.length - 1))
    } else {
      str
    }
  }

  private def isQuoted(str: String): Boolean = {
    val SINGLE_QUOTE = "\'"
    val DOUBLE_QUOTE = "\""
    str.startsWith(SINGLE_QUOTE) && str.endsWith(SINGLE_QUOTE) || str.startsWith(DOUBLE_QUOTE) && str.endsWith(DOUBLE_QUOTE)
  }
}
