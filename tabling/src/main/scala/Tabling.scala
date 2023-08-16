import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

import java.io.IOException

object Tabling extends ZIOAppDefault {

  def readLine(message: String): IO[IOException, String] =
    zio.Console.readLine(message)

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database.Service]

    // 예약 가능한 전체 식당 조회해서 출력
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select id, name
                |from restaurant""".stripMargin
            .query[Restaurant]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

    restaurant_id <- readLine("식당을 선택해주세요 : ")
    isExist <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select EXISTS(select * from restaurant where id = ${Integer
            .parseInt(restaurant_id)})""".stripMargin
            .query[Boolean]
            .unique
        }
      } yield res)

    // 식당 잘못 선택 시 종료
    _ <- ZIO.when(!isExist)({
      zio.Console.printLine("잘못 선택하셨습니다. 프로그램을 종료합니다.")
      sys.exit(-1)
    })

    // 예약 정보 입력
    reservation <- readReservation(restaurant_id)

    insertResult <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|insert into reservation values
                |(${reservation.name},
                |${reservation.phone},
                |${reservation.restaurant_id},
                |${reservation.reservation_date},
                |${reservation.reservation_time},
                |${reservation.guests})""".stripMargin
            .update()
            .run
        }
      } yield res)

    _ <- insertResult match {
      case 1 => zio.Console.printLine(s"예약이 완료되었습니다. $reservation")
      case _ => zio.Console.printLine(s"예약하다 뭔가 문제가 있어요.")
    }
  } yield ()

  def readReservation(
      restaurant_id: String
  ): ZIO[Any, IOException, Reservation] = for {
    name <- readLine("예약자 이름을 입력해주세요 : ")
    phone <- readLine("예약자 전화번호 뒤 4자리를 입력해주세요 : ")
    reservation_date <- readLine("예약 날짜를 입력해주세요 (ex:0730) : ")
    reservation_time <- readLine("예약 시간을 입력해주세요 (ex:1430) : ")
    guests <- readLine("인원 수를 숫자로 입력해주세요 : ")

    reservation = Reservation(
      name,
      phone,
      Integer.parseInt(restaurant_id),
      reservation_date,
      reservation_time,
      Integer.parseInt(guests)
    )
  } yield reservation

  override def run = prog
    .provide(
      conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
    )
    .exitCode

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        s"jdbc:postgresql://DB_PATH/DB_NAME?user=DB_USER&password=DB_PASSWORD"
      )
    )
  )
}
