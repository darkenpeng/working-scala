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
    data = friendList.toJson
    // TODO path 하드코딩...더 나은 방법이 있을지 찾아보기...
    wd = os.pwd / "filter-example" / "src" / "main" / "scala" / "friends.json"
    res <- ZIO
      .attempt(os.write(wd, data))
      .catchAll(cause => ZIO.fail(SimpleError.WriteFail(cause)))

  } yield ()
}
