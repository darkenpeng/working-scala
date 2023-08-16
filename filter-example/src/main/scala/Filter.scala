import zio._
import zio.json._

object Filter extends ZIOAppDefault {

  def readJson(name: String): ZIO[Any, SimpleError, String] =
    for {
      _ <- Console.printLine(s"read ${name}").ignore
      wd = os.pwd / "filter-example" / "src" / "main" / "scala"
      json <- ZIO
        .attempt(ujson.read(os.read(wd / s"$name")))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json.toString()
  override def run =
    for {
      data <- readJson("friends.json")
      friendList <- ZIO.fromEither(data.fromJson[List[Friend]])
      _ <- zio.Console.printLine(friendList)

    } yield ()
}

case class GoodFriend(name: String, age: Int)
