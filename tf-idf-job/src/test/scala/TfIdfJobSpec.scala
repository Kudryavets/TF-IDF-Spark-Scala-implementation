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
          "id_i\tword_i word_i word_ii word_iii",
          "id_ii\tword_ii word_iii word_iv",
          "id_iii\tword_iii word_iii word_iv word_v"
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
          |}
        """.stripMargin
      )
      val cliConfig: CliConfig = CliConfig()
      
      val job = new TfIdfJob(sparkSession, config, cliConfig)
  
      val result = job.computeInvertedIndex(textCorpus)
      result.collect() should contain theSameElementsAs Seq(
        "word_i  [{id_i:0.5493}]",                    // tf {id_i: 0.5}                              idf 1.0986
        "word_ii  [{id_i:0.10125},{id_ii:0.1215}]",   // tf {id_i: 0.25, id_ii: 0.3(3)}              idf 0.405
        "word_iii  [{id_i:0},{id_ii:0}]",             // tf {id_i: 0.25, id_ii: 0.3(3), id_iii: 0.5} idf 0
        "word_iv  [{id_ii:0.10125},{id_iii:0.1215}]", // tf {id_ii: 0.3(3), id_iii: 0.25}            idf 0.405
        "word_v  [{id_iii:0.5493}]"                   // tf (id_iii: 0.25}                           idf 1.0986
      )
    }
  }
}
