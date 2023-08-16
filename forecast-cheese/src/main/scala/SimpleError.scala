abstract class SimpleError(message: String = "", cause: Throwable = null)
    extends Throwable(message, cause)
    with Product
    with Serializable
object SimpleError {
  final case class ReadFail(cause: Throwable)
      extends SimpleError(s"read fail: ", cause)
}
