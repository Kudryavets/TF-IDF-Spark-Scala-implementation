package textProcessors

trait TextParser {
  def apply(text: String): String
}

object DummyTextParser extends TextParser {
  def apply(text: String): String = ???
  // remove punctuation
  // remove digits
  // toLoverCase
}
