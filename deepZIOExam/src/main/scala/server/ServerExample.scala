package server

import zio._
import zio.http._
import zio.json._

object ServerExample extends ZIOAppDefault {

  val app =
    Http.collectZIO[Request] { case Method.GET -> Root / "reporting-test" =>
      for {
        age <- Random.nextIntBounded(13)
        hobbies = List("공차기", "요리하기", "스쿠터", "코딩")
        shuffledHobbies <- Random.shuffle(hobbies)
        firstHobbies = shuffledHobbies.take(2)
        friend: Friend = Friend("익명", age, firstHobbies, "비밀의장소")
        _ <- ZIO.sleep(2.second)
        res <- ZIO.succeed(Response.text(friend.toJson))
        _ <- zio.Console.printLine(s"2초 쉬고 보냈습니다 >>> $friend")
      } yield res
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

  override val run =
    Server
      .serve(app.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
}
