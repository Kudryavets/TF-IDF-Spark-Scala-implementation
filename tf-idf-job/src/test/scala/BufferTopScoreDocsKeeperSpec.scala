import org.scalatest.{Matchers, WordSpec}


class BufferTopScoreDocsKeeperSpec extends WordSpec with Matchers {
  "BufferTopHolder" should {
    "addElement correctly" when {
      "buffer is empty" in {
        val buffer = new BufferTopScoreDocsKeeper(3)
  
        buffer.sequenceOp(("some_word", 1.0)).flush() shouldEqual List(("some_word", 1.0))
      }
      
      "buffer is not empty and has size < topDocsLimit" in {
        val buffer = new BufferTopScoreDocsKeeper(3)
        
        val res = (1 to 3)
          .map(i => (s"some_word_$i", i/2.0))
          .foldLeft(buffer)((acc, el) => buffer.sequenceOp(el))
          .flush()
        
        res shouldEqual List(("some_word_1", 0.5), ("some_word_2", 1.0), ("some_word_3", 1.5))
      }
      
      "buffer is not empty and has size = topDocsLimit" in {
        val buffer = new BufferTopScoreDocsKeeper(3)
  
        val res = (1 to 4)
          .map(i => (s"some_word_$i", i/2.0))
          .foldLeft(buffer)((acc, el) => buffer.sequenceOp(el))
          .flush()
  
        res shouldEqual List(("some_word_2", 1.0), ("some_word_3", 1.5), ("some_word_4", 2))
      }
    }
    
    "merge another bufferCorrectly" in {
      var bufferLft = new BufferTopScoreDocsKeeper(3)
      bufferLft = (1 to 4)
        .map(i => (s"some_word_$i", i/2.0))
        .foldLeft(bufferLft)((acc, el) => bufferLft.sequenceOp(el))
  
      var bufferRght = new BufferTopScoreDocsKeeper(3)
      bufferRght = (5 to 6)
        .map(i => (s"some_word_$i", i/2.0))
        .foldLeft(bufferRght)((acc, el) => bufferRght.sequenceOp(el))
        .sequenceOp(("some_word_0", 0/2.0))
      
      bufferLft.combineOp(bufferRght).flush() shouldEqual List(
        ("some_word_4", 2.0), ("some_word_5", 2.5), ("some_word_6", 3.0)
      )
    }
  }
}
