package Data

class DataException(message: String = null, cause: Throwable = null) extends
  RuntimeException(DataException.defaultMessage(message, cause), cause){}

object DataException {
  def defaultMessage(message: String, cause: Throwable) =
    if (message != null) message
    else if (cause != null) cause.toString()
    else null
}


