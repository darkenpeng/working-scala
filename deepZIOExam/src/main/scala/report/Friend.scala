package report

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

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
