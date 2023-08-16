package report

import sttp.client3.ziojson.asJson
import sttp.client3.{
  HttpClientSyncBackend,
  Identity,
  SttpBackend,
  UriContext,
  basicRequest
}
import zio.json.DecoderOps
import zio.{ZIO, ZIOAppDefault}

object Reporting extends ZIOAppDefault {
  val path = os.pwd / "fixture"
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  val prog = for {
    _ <- ZIO.unit
    backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    response = basicRequest
      .get(uri"http://localhost:13333/reporting-test")
      .response(asJson[Friend])
      .send(backend)

    f <- response.body match {
      case Left(_) => ZIO.fail(new Exception("fail"))
      case Right(friend) =>
        println(friend)
        ZIO.succeed(friend)
    }
  } yield f

  override def run = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / "friends.txt")))
      .catchAll(e => ZIO.fail(new Exception(s"Failed to parse json: $e")))

    jsonString = json.toString()
    eitherFriends = jsonString.fromJson[List[Friend]]
    friends <- ZIO.fromEither(eitherFriends)
    // ServerExample을 통해 랜덤 친구를 얻으려면 아래 주석 해제
//    friends <- ZIO.foreach(1 to 5){ _ =>
//      prog.debug("새 친구")
//    }
    _ <- zio.Console.printLine(friends)
    ageSum = friends.map(_.age).sum
    _ <- zio.Console.printLine(s"나이 합계는 $ageSum")
  } yield ()
}
