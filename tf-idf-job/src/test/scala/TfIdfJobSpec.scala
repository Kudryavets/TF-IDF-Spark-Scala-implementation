import TfIdfJob.CliConfig
import com.typesafe.config.Config
import org.scalatest.{Matchers, WordSpec}

class TfIdfJobSpec extends WordSpec
  with Matchers
  with AbstractSparkTest {
  
  "TfIdfJob" should {
    "compute inverted index correctly" in {
      val textCorpus = sparkSession.sparkContext.parallelize(
        Seq(
          ???
        )
      )
  
      val config: Config = ???
      val cliConfig: CliConfig = ???
      
      val job = new TfIdfJob(sparkSession, config, cliConfig)
      
      val result = job.computeInvertedIndex(???)
      result.collect() should contain theSameElementsAs Seq(
        ???
      )
    }
  }
}
