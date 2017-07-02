import TfIdfJob.CliConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import textProcessors.{DummyTextParser, SimpleWordCounter}
import util.{JsonUtils, LineBuilder}


/**
  * Main class for TF-IDF computation.
  *
  * It gets initial text corpus from [[CliConfig]], preprocesses text with [[DummyTextParser]] and counts words in each document
  * with the help of [[SimpleWordCounter]]. Then rdd is flatten to (word -> document id -> tf) structure, repartitioned
  * by word and cached in serialized form in memory and on disc. Thus we can avoid unnecessary shuffles and recalculations.
  *
  * Idf rdd is computed on the base of cached tf rdd  and then joined to the tf rdd to get (word -> document id ->
  * tf-idf score) rdd. After that result rdd efficiently aggregated with [[BufferTopKeeper]] which allows to keep in memory only
  * required amount of elements with highest tf-idf score.
  *
  * Alternatively we could aggregate tf rdd by word and then compute tf-idf score in each group, but spark better works with lots
  * of small records then with small amount of large records. Hence our approach is more sustainable, and smart partitioning
  * prevents us from expensive join.
  *
  * @param spark The entry point to programming Spark
  * @param config Permanent config from config file (environment specific, job specific)
  * @param cliConfig Сhangeable сonfig from command line (launch specific)
  */
class TfIdfJob(spark: SparkSession,
               config: Config,
               cliConfig: CliConfig) {
  
  def run(): Unit = {
    val initialRdd = spark.sparkContext.textFile(cliConfig.inputFolder)
  
    computeInvertedIndex(initialRdd)
      .saveAsTextFile(cliConfig.outputFolder)
  }
  
  def computeInvertedIndex(initialRdd: RDD[String]): RDD[String] = {
    val docCount = initialRdd.count().toDouble
  
    val wordCounter = new SimpleWordCounter(DummyTextParser)
    val wordCounterBr = spark.sparkContext.broadcast(wordCounter)
  
    val partitioner = new HashPartitioner(config.getInt("tfIdfJobParams.tfRdd.numPartitions"))
  
    val tfRdd = initialRdd.flatMap { line =>
      val Array(docId, text) = line.split("\t", 2)
      val wordCounts = wordCounterBr.value(text)
      val textLength = wordCounts.values.sum.toDouble
    
      wordCounts.mapValues(docId -> _ / textLength)
    }.partitionBy(partitioner)
      .persist(StorageLevel.MEMORY_AND_DISK_SER)
    
    val DEFAULT_COUNT = 1
    val idfRdd = tfRdd
      .mapValues(_ => DEFAULT_COUNT)
      .reduceByKey(_ + _)
      .mapValues(wordDocCount => math.log(docCount / wordDocCount))
  
    val relevanceListSize = config.getInt("tfIdfParams.relevance.list.size")
    val invertedIndexRdd = tfRdd.join(idfRdd)
      .mapValues{ case ((docId, tfScore), idfScore) => docId -> tfScore * idfScore }
      .aggregateByKey(new BufferTopKeeper(relevanceListSize)) (
        (acc, el) => acc.addElement(el),
        (accLeft, accRight) => accLeft.mergeBuffer(accRight)
      )
  
    invertedIndexRdd
      .mapValues(_.flush().map{ case (docId, score) =>
        docId -> BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_DOWN).toDouble})
      .mapValues(JsonUtils.toJson)
      .map{ case (id, relevance) => LineBuilder.prettify(id, relevance) }
  }
}

object TfIdfJob extends AbstractJob {
  
  case class CliConfig(inputFolder: String = null, outputFolder: String = null)
  
  val cliParser = new scopt.OptionParser[CliConfig](getClass.getSimpleName) {
    opt[String]('i', "inputFolder")
      .required()
      .valueName("<inputFolder>")
      .action((x, c) => c.copy(inputFolder = x))
      .text("Input folder to load text corpus from")
    
    opt[String]('o', "outputFolder")
      .required()
      .valueName("<outputFolder>")
      .action((x, c) => c.copy(outputFolder = x))
      .text("Otput folder to save invert index to")
  }
  
  def doWork(spark: SparkSession, args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    
    cliParser.parse(args, CliConfig()).foreach { cliConfig =>
      
      val job = new TfIdfJob(
        spark,
        config,
        cliConfig
      )
      
      job.run()
    }
  }
}
