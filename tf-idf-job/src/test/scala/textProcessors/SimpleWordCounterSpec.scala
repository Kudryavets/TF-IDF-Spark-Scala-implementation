package textProcessors

import org.scalatest.{Matchers, WordSpec}

class SimpleWordCounterSpec extends WordSpec with Matchers {
  "SimpleWordCounter with DummyTextParser" should {
    "process text correctly" in {
      val text = ???
  
      val wordCounter = new SimpleWordCounter(DummyTextParser)
  
      wordCounter(text) shouldEqual Map(
      
      )
    }
  }
}
