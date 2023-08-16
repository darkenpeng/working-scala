import zio._
import zio.Console
import zio.json._

case class Todo(title: String, text: String, completed: Boolean)

object Todo {
  implicit val decoder: JsonDecoder[Todo] = DeriveJsonDecoder.gen[Todo]
  implicit val encoder: JsonEncoder[Todo] = DeriveJsonEncoder.gen[Todo]
}

object Main extends ZIOAppDefault {
  def decode(string: String): Task[Todo] = {
    string.fromJson[Todo] match {
      case Left(_)      => ZIO.fail(new Exception("todo decode fail"))
      case Right(value) => ZIO.succeed(value)
    }
  }

  def run = {
    for {
      _ <- Console.printLine("json round trip")
      todo = Todo("study", "zio-json study", completed = false)

      encodeResult = todo.toJson
      _ <- Console.printLine(s"encode result: $encodeResult")
      // encode result: {"title":"study","text":"zio-json study","completed":false}

      decodeResult <- decode(encodeResult)
      _ <- Console.printLine(s"decode result: $decodeResult")
      // decode result: Todo(study,zio-json study,false)

      decodeFailResult <- decode("something").catchAll(_ =>
        ZIO.succeed(Todo("", "", completed = false))
      )
      _ <- Console.printLine(s"decode fail result: $decodeFailResult")
      // decode result: Todo(,,false)
    } yield ()
  }
}
