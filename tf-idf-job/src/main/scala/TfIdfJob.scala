import TfIdfJob.CliConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import textProcessors.{DummyTextParser, SimpleWordCounter}
import util.{JsonUtils, LineBuilder}


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
  
    val partitioner = new HashPartitioner(config.getInt("tfIdfJobParams.documentWordCountRdd.numPartitions"))
  
    val documentWordCountRdd = initialRdd.flatMap { line =>
      val Array(docId, text) = line.split("\t")
      val wordCounts = wordCounterBr.value(text)
    
      wordCounts.mapValues(docId -> _)
    }.partitionBy(partitioner)
      .persist(StorageLevel.MEMORY_AND_DISK_SER)
  
    val DEFAULT_COUNT = 0
    val corpusWordCountRdd = documentWordCountRdd
      .mapValues(_ => DEFAULT_COUNT)
      .reduceByKey(_ + _)
      .mapValues(corpusWordCount => math.log(corpusWordCount/docCount))
  
    val relevanceListSize = config.getInt("tfIdfParams.relevance.list.size")
    val invertedIndexRdd = documentWordCountRdd.join(corpusWordCountRdd)
      .mapValues{ case ((docId, docWordCount), corpusWordCount) => docId -> docWordCount / corpusWordCount }
      .aggregateByKey(new BufferTopHolder(relevanceListSize)) (
        (acc, el) => acc.addElement(el),
        (accLeft, accRight) => accLeft.mergeBuffer(accRight)
      )
  
    invertedIndexRdd
      .mapValues(_.flush())
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
