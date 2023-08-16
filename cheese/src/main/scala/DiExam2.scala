import zio.{ZIO, ZIOAppDefault, ZLayer}

import java.io.IOException

object DiExam2 extends ZIOAppDefault {

  trait 식당저장소 {
    def 모든식당이름가져오기(): ZIO[Any, Nothing, List[String]]

    def 식당메뉴가져오기(name: String): ZIO[Any, Nothing, List[String]]
  }

  class 공덕식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("김밥집", "피자집", "샌드위치집"))

    def 식당메뉴가져오기(name: String) = ZIO.succeed(List("참치김밥", "계란김밥", "매운김밥"))
  }

  class 판교식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("카카오국밥", "네이버국밥", "엔씨국밥"))

    def 식당메뉴가져오기(name: String) = ZIO.succeed(List("라이언국밥", "춘식국밥"))
  }

  object 판교식당저장소 {
    val layer = ZLayer.succeed(new 판교식당저장소)
  }

  def 예약정보등록하기(input: String) = ZIO.succeed(true)

  // 2. 여기서 자유롭게 식당저장소를 쓸 수 있어요
  def useCase(repo: 식당저장소) = for {
    _ <- ZIO.unit
    list <- repo.모든식당이름가져오기()
    _ <- zio.Console.printLine(list)
    input <- zio.Console.readLine("입력 : ")
    _ <- 예약정보등록하기(input)
  } yield ()

  def useCaseUsingZLayer(): ZIO[식당저장소, IOException, Unit] =
    for { // 항상 Any로 있던게 식당저장소가 됐다... 의존성과 진짜 input을 분리할 수 있게됨!!!
      repo <- ZIO.service[식당저장소]
      _ <- ZIO.unit
      list <- repo.모든식당이름가져오기()
      _ <- zio.Console.printLine(list)
      input <- zio.Console.readLine("입력 : ")
      _ <- 예약정보등록하기(input)
    } yield ()

  override def run = for {
    _ <- ZIO.unit
    _ <- useCase(new 공덕식당저장소) // 1. 의존성을 넣어주면
    _ <- useCaseUsingZLayer().provideLayer(판교식당저장소.layer)
  } yield ()
}
