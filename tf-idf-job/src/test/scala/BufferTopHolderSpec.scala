import org.scalatest.{Matchers, WordSpec}


class BufferTopHolderSpec extends WordSpec with Matchers {
  "BufferTopHolder" should {
    "addElement correctly" when {
      "buffer is empty" in {
        val buffer = new BufferTopHolder(3)
  
        buffer.addElement(("some_word", 1.0)).flush() shouldEqual List(("some_word", 1.0))
      }
      
      "buffer is not empty and has size < topDocsLimit" in {
        val buffer = new BufferTopHolder(3)
        
        val res = (1 to 3)
          .map(i => (s"some_word_$i", i/2.0))
          .foldLeft(buffer)((acc, el) => buffer.addElement(el))
          .flush()
        
        res shouldEqual List(("some_word_1", 0.5), ("some_word_2", 1.0), ("some_word_3", 1.5))
      }
      
      "buffer is not empty and has size = topDocsLimit" in {
        val buffer = new BufferTopHolder(3)
  
        val res = (1 to 4)
          .map(i => (s"some_word_$i", i/2.0))
          .foldLeft(buffer)((acc, el) => buffer.addElement(el))
          .flush()
  
        res shouldEqual List(("some_word_2", 1.0), ("some_word_3", 1.5), ("some_word_4", 2))
      }
    }
    
    "merge another bufferCorrectly" in {
      var bufferLft = new BufferTopHolder(3)
      bufferLft = (1 to 4)
        .map(i => (s"some_word_$i", i/2.0))
        .foldLeft(bufferLft)((acc, el) => bufferLft.addElement(el))
  
      var bufferRght = new BufferTopHolder(3)
      bufferRght = (5 to 6)
        .map(i => (s"some_word_$i", i/2.0))
        .foldLeft(bufferRght)((acc, el) => bufferRght.addElement(el))
        .addElement(("some_word_0", 0/2.0))
      
      bufferLft.mergeBuffer(bufferRght).flush() shouldEqual List(
        ("some_word_4", 2.0), ("some_word_5", 2.5), ("some_word_6", 3.0)
      )
    }
  }
}
