import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

trait AbstractSparkTest extends BeforeAndAfterAll { this: Suite =>
  var sparkSession: SparkSession = _
  
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    
    sparkSession = SparkSession.builder()
      .appName(this.getClass.getSimpleName)
      .master("local")
      .config("spark.ui.enabled", "false")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.hadoop.validateOutputSpecs", "false")
      .getOrCreate()
  }
  
  override protected def afterAll(): Unit = {
    super.afterAll()
    sparkSession.stop()
  }
}
