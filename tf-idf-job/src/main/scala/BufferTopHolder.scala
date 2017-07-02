class BufferTopHolder(topDocsLimit: Int) {
  require(topDocsLimit > 0, s"InvalidArgument: top in BufferTopHolder required to be > 0 but found $topDocsLimit")
  
  type DocRating = (String, Double)
  
  private var buffer: List[DocRating] = List.empty[DocRating]
  
  private def addElementRec(buf: List[DocRating], el: DocRating): List[DocRating] = buf match {
    case Nil => el :: Nil
    case h :: tail => if(el._2 < h._2) el :: h :: tail else h :: addElementRec(tail, el)
  }
  
  
  def addElement(el: DocRating): BufferTopHolder = {
    val newBuffer = addElementRec(buffer, el)
    
    if (newBuffer.size <= topDocsLimit) {
      buffer = newBuffer
    } else {
      buffer = newBuffer.tail
    }
    
    this
  }
  
  def mergeBuffer(rght: BufferTopHolder): BufferTopHolder = rght.flush().foldLeft(this)((buf, el) => this.addElement(el))
  
  def flush(): List[DocRating] = buffer
}
