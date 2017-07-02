import TfIdfJob.CliConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}

class TfIdfJobSpec extends WordSpec
  with Matchers
  with AbstractSparkTest {
  
  "TfIdfJob" should {
    "compute inverted index correctly" in {
      val textCorpus = sparkSession.sparkContext.parallelize(
        Seq(
          "idi\twordi wordi wordii wordiii",
          "idii\twordii wordiii wordiv",
          "idiii\twordiii wordiii wordiv wordv"
        )
      )
  
      val config: Config = ConfigFactory.parseString(
        """
          |tfIdfJobParams: {
          |  tfRdd.numPartitions: 2
          |}
          |
          |tfIdfParams: {
          |  relevance.list.size: 2
          |  scorePrecision: 4
          |}
        """.stripMargin
      )
      val cliConfig: CliConfig = CliConfig()
      
      val job = new TfIdfJob(sparkSession, config, cliConfig)
  
      val result = job.computeInvertedIndex(textCorpus)
      result.collect() should contain theSameElementsAs Seq(
        "wordi  [{\"idi\":0.5493}]",                      // tf {idi: 0.5}                            idf 1.0986
        "wordii  [{\"idi\":0.1014},{\"idii\":0.1352}]",   // tf {idi: 0.25, idii: 0.3(3)}             idf 0.405465
        "wordiii  [{\"idii\":0.0},{\"idiii\":0.0}]",      // tf {idi: 0.25, idii: 0.3(3), idiii: 0.5} idf 0
        "wordiv  [{\"idiii\":0.1014},{\"idii\":0.1352}]", // tf {idii: 0.3(3), idiii: 0.25}           idf 0.405465
        "wordv  [{\"idiii\":0.2747}]"                     // tf (idiii: 0.25}                         idf 1.0986
      )
    }
  }
}
