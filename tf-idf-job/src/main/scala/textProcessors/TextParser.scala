package textProcessors


/**
  * Class that implements text preprocessing, removing irrelevant characters, lemmatization and etc.
  */
trait TextParser {
  def apply(text: String): String
}


/**
  * Removes punctuation and digits from the text, coerces to lover case.
  */
object DummyTextParser extends TextParser {
  def apply(text: String): String = text
    .replaceAll("""[\p{Punct}]""", " ")
    .filter(!_.isDigit)
    .trim()
    .replaceAll(" +", " ")
    .toLowerCase()
}
