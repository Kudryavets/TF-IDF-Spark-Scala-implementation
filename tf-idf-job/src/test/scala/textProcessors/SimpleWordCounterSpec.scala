package textProcessors

import org.scalatest.{Matchers, WordSpec}

class SimpleWordCounterSpec extends WordSpec with Matchers {
  "SimpleWordCounter with DummyTextParser" should {
    "process text correctly" in {
      val text = "Lorem ipsum dolor sit amet, 2222consectetur adipiscing elit; 777! sed do 2 eiusmod tempor incididunt ut dolor " +
        "Lorem ipsum dolor. "
  
      val wordCounter = new SimpleWordCounter(DummyTextParser)
  
      wordCounter(text) shouldEqual Map(
        "lorem" -> 2,
        "ipsum" -> 2,
        "dolor" -> 3,
        "sit" -> 1,
        "amet" -> 1,
        "consectetur" -> 1,
        "adipiscing" -> 1,
        "elit" -> 1,
        "sed" -> 1,
        "do" -> 1,
        "eiusmod" -> 1,
        "tempor" -> 1,
        "incididunt" -> 1,
        "ut" -> 1
      )
    }
  }
}
