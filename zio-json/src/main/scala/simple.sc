import zio.json._

case class Banana(curvature: Double)

object Banana {
  implicit val decoder: JsonDecoder[Banana] = DeriveJsonDecoder.gen[Banana]
  implicit val encoder: JsonEncoder[Banana] = DeriveJsonEncoder.gen[Banana]
}

"""{"curvature":0.5}""".fromJson[Banana]
// val res: Either[String,Banana] = Right(Banana(0.5))

Banana(0.5).toJson
// val res: String = {"curvature":0.5}

Banana(0.5).toJsonPretty
//val res: String =
//{
//  "curvature" : 0.5
//}
