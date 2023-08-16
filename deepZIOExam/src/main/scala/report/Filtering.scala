package report

import zio.json.DecoderOps
import zio.{ZIO, ZIOAppDefault}

object Filtering extends ZIOAppDefault {
  val path = os.pwd / "fixture"
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  def filterHobbies(
      hobbies: List[String],
      filters: List[HobbyFilter]
  ): Boolean = {
    val hobbiesStr = hobbies.toString()
    filters.map(filter => hobbiesStr.contains(filter.hobby)).exists(x => x)
  }

  override def run = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / "friends.txt")))
      .catchAll(e => ZIO.fail(new Exception(s"Failed to parse json: $e")))

    eitherFriends = json.toString().fromJson[List[Friend]]
    friends <- ZIO.fromEither(eitherFriends)
    _ <- zio.Console.printLine(s"친구들 >>> $friends")

    filters = List(HobbyFilter("코딩"), HobbyFilter("요리하기"))
    goodFriends = friends
      .filter(f => filterHobbies(f.hobbies, filters))
      .map(f => GoodFriend(f.name, f.age))
    _ <- zio.Console.printLine(s"좋은 친구들 >>> $goodFriends")
  } yield ()
}
