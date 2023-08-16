import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class TestTableRow(name: String, hobby: String)

object DBSampleApp extends ZIOAppDefault {

  def insertTableRow(row: TestTableRow): ZIO[Database, Throwable, Int] = {
    val insertQuery = tzio {
      sql"""|insert into test_table (name, hobby)
              |values (${row.name}, ${row.hobby})""".stripMargin.update.run
    }

    val db = ZIO.service[Database]
    db.flatMap(database => database.transactionOrWiden(insertQuery))
  }

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    _ <- insertTableRow(TestTableRow("John", "Skiing"))
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select name, hobby
                  |from zombie""".stripMargin
            .query[TestTableRow]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

  } yield ()
  override def run = prog.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

  val sqlite = locally {
    val path = SimpleUtil.path
    s"jdbc:sqlite:$path"
  }

  val postgres = locally {
    val path = "localhost:5432"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        sqlite
      )
    )
  )
}
