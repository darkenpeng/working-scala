import zio._
import sttp.client3._
import zio.json._
import sttp.client3.ziojson._
import ujson.Value.Value

import java.time.{LocalDate, LocalTime}

object ForecastClient extends ZIOAppDefault {

  case class RequestPayload(content: String)
  case class ResponsePayload(data: String)

  private val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  private val today = LocalDate.now.toString
  private val now = LocalTime.now.toString.replaceAll(":", "").substring(0, 4)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Throwable, Unit] = {
    for {
      _ <- ZIO.unit
      regions <- ZIO.succeed(getRegions)
      messages <- ZIO.foreach(regions) { region =>
        for {
          _ <- ZIO.unit
          body = getApiResponse(region.forecastApiUrl(today, now)).body
          json <- readJson(body).mapError(SimpleError.ReadFail)
          items <- getItemsFromJson(json)
          message = s"> **${region.name} 날씨($today)** \n$items"
        } yield message
      }
      discordMessage = messages.mkString("\n")
      _ <- sendDiscordMessage(discordMessage)
    } yield ()
  }

  def getRegions: List[Region] = {
    List(
      Region(60, 126, "공덕동"),
      Region(59, 125, "독산동"),
      Region(57, 125, "범박동"),
      Region(61, 125, "대치동")
    )
  }

  private def getApiResponse(url: String): Identity[Response[String]] = {
    basicRequest
      .get(uri"$url")
      .response(asString.getRight)
      .send(backend)
  }

  def readJson(name: String): ZIO[Any, SimpleError.ReadFail, Value] =
    for {
      json <- ZIO
        .attempt(ujson.read(name))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json

  def getItemsFromJson(json: Value): ZIO[Any, Throwable, String] = {
    ZIO
      .attempt {
        val items = json("response")("body")("items")("item").arr
        items
          .filter(item => item.obj("category").str == "RN1")
          .map(item => convertItemToMessage(item))
          .mkString
      }
      .catchAll(_ => { ZIO.succeed("> 정보 없음\n") })
  }

  def convertItemToMessage(item: Value): String = {
    s"> ${item.obj("fcstTime").str.substring(0, 2)}시 : ${item.obj("fcstValue").str} \n"
  }

  def sendDiscordMessage(message: String): ZIO[Any, Exception, Identity[
    Response[Either[ResponseException[String, String], ResponsePayload]]
  ]] = {
    val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    implicit val payloadJsonEncoder: JsonEncoder[RequestPayload] =
      DeriveJsonEncoder.gen[RequestPayload]
    implicit val myResponseJsonDecoder: JsonDecoder[ResponsePayload] =
      DeriveJsonDecoder.gen[ResponsePayload]

    val requestPayload = RequestPayload(message)
    val response: Identity[
      Response[Either[ResponseException[String, String], ResponsePayload]]
    ] =
      basicRequest
        .post(uri"${MyKeyUtil.discordUri}")
        .body(requestPayload)
        .response(asJson[ResponsePayload])
        .send(backend)

    ZIO
      .attempt(response)
      .catchAll(_ => ZIO.fail(new Exception(s"discord send fail")))
  }
}
