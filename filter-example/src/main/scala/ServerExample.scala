import zio._
import zio.http._

import ujson.Value.Value

import java.io.IOException

sealed trait SimpleError extends Throwable with Product with Serializable
object SimpleError {
  case class ReadFail(cause: Throwable) extends SimpleError
  case class FindFriendsFail(cause: Throwable) extends SimpleError
  case class WriteFail(cause: Throwable) extends SimpleError
}
sealed trait SimpleReport extends Product with Serializable
object SimpleReport {
  case class FailGenerateReport(cause: SimpleError) extends SimpleReport
  case class SuccessGenerateReport(message: String) extends SimpleReport
}

object ServerExample extends ZIOAppDefault {
  val path = os.pwd / "fixture"
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  def readJson(name: String): ZIO[Any, SimpleError, String] =
    for {
      _ <- Console.printLine(s"read ${name}").ignore
      json <- ZIO
        .attempt(ujson.read(os.read(path / s"$name")))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json.toString()

  val app =
    Http.collectZIO[Request] { case Method.GET -> Root / "reporting-test" =>
      for {
        _ <- zio.Console.printLine("/reporting-test endpoint!")
        data <- readJson("friends.txt")
        _ <- zio.Console.printLine(data)
      } yield Response.json(data)
    }

  override val run =
    Server
      .serve(app.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
}
