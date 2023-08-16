import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class Zombie(name: String)

object ZombieGame extends ZIOAppDefault {
  //val config = ScalafmtConfig.fromHoconFile("./scalafmt.conf")

  def getTargetName(rows: List[Zombie], choice: Integer) = for {
    result <- ZIO
      .attempt(rows(choice).name)
      .catchAll(cause => ZIO.succeed(""))
  } yield result

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select name
              |from zombie""".stripMargin
            .query[Zombie]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

    input <- zio.Console.readLine(s"좀비 번호 입력(1~${rows.size}) : ")
    nInput = Integer.parseInt(input) - 1

    target <- getTargetName(rows, nInput)
    _ <- target match {
      case "" => zio.Console.printLine("좀비 무서워서 도망갔다!")
      case _ =>
        for {
          _ <- database
            .transactionOrWiden(for {
              res <- tzio {
                sql"""|delete from zombie where name = ${target}""".stripMargin.update.run
              }
            } yield res)
          _ <- zio.Console.printLine(s"${target}을 손봐줬다!")
        } yield ()
    }
  } yield ()

  override def run = prog.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

  val sqlite = locally {
    val path = SimpleUtil.path
    s"jdbc:sqlite:$path"
  }

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        sqlite
      )
    )
  )
}
