package textProcessors

import textProcessors.SimpleWordCounter._


/**
  * Class for text processing. Computes amount of words appearances in a text.
  *
  * @param textParser Object that implements text preprocessing, removing irrelevant characters, lemmatization and etc.
  */
class SimpleWordCounter(textParser: TextParser) {
  type WordCounter = Map[String, Long]
  
  def apply(text: String): WordCounter = textParser(text).split(" ")
    .foldLeft (Map.empty[String, Long]) (
      (acc , word) => acc.updated(word, acc.getOrElse(word, DEFAULT_COUNT) + DEFAULT_INCREMENT)
    )
}

object SimpleWordCounter {
  private val DEFAULT_COUNT = 0L
  private val DEFAULT_INCREMENT = 1L
}
