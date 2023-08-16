import sttp.client3.ziojson.asJson
import zio._
import sttp.client3._
import zio.json.{
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  EncoderOps,
  JsonDecoder,
  JsonEncoder
}

case class Friend(
    name: String,
    age: Int,
    hobbies: List[String],
    location: String
)

object Friend {
  implicit val decoder: JsonDecoder[Friend] = DeriveJsonDecoder.gen[Friend]
  implicit val encoder: JsonEncoder[Friend] = DeriveJsonEncoder.gen[Friend]
}

object Client extends ZIOAppDefault {
  val prog = for {
    _ <- ZIO.unit
    backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    response = basicRequest
      .get(uri"http://localhost:13333/reporting-test")
      .response(asJson[List[Friend]])
      .send(backend)
    f <- response.body match {
      case Left(exception) => ZIO.fail(exception.getMessage())
      case Right(friendList) => {
        ZIO.succeed(friendList)
      }
    }
  } yield f
  override def run = for {
    friendList <- prog
    _ <- zio.Console.printLine(friendList)
    
  } yield ()

}
