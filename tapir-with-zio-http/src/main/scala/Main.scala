import sttp.tapir.Endpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir._
import zhttp.service.Server
import zio._

// open: http://localhost:8090/hello?name=gusam

object Main extends ZIOAppDefault {
  val myEndpoint: Endpoint[Unit, String, Unit, String, Any] =
    endpoint.get
      .in("hello")
      .in(query[String]("name"))
      .out(stringBody)

  val myHttp =
    ZioHttpInterpreter().toHttp(
      myEndpoint.zServerLogic(name => ZIO.succeed(s"name : $name"))
    )

  override def run = {
    Console.printLine("server start") *>
      Server.start(8090, myHttp)
  }
}
