import doobie.implicits._
import io.github.gaelrenoux.tranzactio.{ConnectionSource, DbException, doobie}
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._

import java.io.IOException

object ChangeReservation extends ZIOAppDefault {

  def readLine(message: String): IO[IOException, String] =
    zio.Console.readLine(message)

  def updateReservation(
      database: doobie.Database.Service,
      target: Reservation
  ): ZIO[Any, DbException, Unit] = for {
    // TODO : 예약 수정 정보를 입력받아서 update
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|update reservation set reservation_time='0000' where name = ${target.name} and phone = ${target.phone}""".stripMargin.update.run
        }
      } yield res)
  } yield ()

  def deleteReservation(
      database: doobie.Database.Service,
      target: Reservation
  ): ZIO[Any, DbException, Unit] = for {
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|delete from reservation where name = ${target.name} and phone = ${target.phone}""".stripMargin.update.run
        }
      } yield res)
  } yield ()

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database.Service]

    name <- readLine("이름을 선택해주세요 : ")
    phone <- readLine("전화번호 뒤 4자리를 입력해주세요 : ")
    reservationList <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select * from reservation where name = $name and phone = $phone""".stripMargin
            .query[Reservation]
            .to[List]
        }
      } yield res)

    // 입력한 정보에 해당하는 예약이 있을 경우만 수정/삭제 가능
    _ <- reservationList.size match {
      case 0 => zio.Console.printLine("입력하신 정보에 해당하는 예약이 없습니다.")
      case _ =>
        for {
          // TODO: 예약 리스트 출력 포맷 변경 (1,2,3, ... 예약 인덱스)
          _ <- zio.Console.printLine(reservationList)
          choice <- readLine("수정/삭제하실 예약 번호를 선택하세요 : ")
          target = reservationList(Integer.parseInt(choice))
          func <- readLine("수정(1) / 삭제(2) : ")
          _ <- func match {
            case "1" => updateReservation(database, target)
            case "2" => deleteReservation(database, target)
          }
        } yield ()
    }
  } yield ()

  override def run = prog
    .provide(
      conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
    )

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        s"jdbc:postgresql://DB_PATH/DB_NAME?user=DB_USER&password=DB_PASSWORD"
      )
    )
  )
}
