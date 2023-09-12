# working-scala

## 설명
- 하나의 프로젝트는 하나 또는 서로 관련된 두 개 이상의 주제에 대한 예시 코드로 구성되어 있습니다.
- 각 주제에 대한 설명은 프로젝트 디렉토리 하위의 README.md 를 참조하시길 바랍니다.

### 프로젝트 별 기여자

형식) `프로젝트-이름` @기여자-이름

`db-connection` @Jihun @Whis-dev

`declarative-prog` @darkenpeng

`dependency-injection` @Jihun

`error-handling` @Whis-dev

`for-comprehension-in-scala` @hatchling13

`for-comprehension-in-zio` @hatchling13

`json-handling` @junghoon-vans

`multi-project` @ycheese

`testing` @ycheese

`webhook` @notJoon

## 프로젝트 별 실행방법

예제코드의 실행 결과를 보려면 각 프로젝트의 Main Class를 실행해주세요.
경로 : ~/working-scala/

  ### sbt로 실행
    sbt
    project `프로젝트 이름`
    run
  [스칼라 공식문서 - getting-started](https://docs.scala-lang.org/getting-started/index.html)

  ### inteliJ로 실행
  [jetbrain 공식문서 - run scala app](https://www.jetbrains.com/help/idea/run-debug-and-test-scala.html#run_scala_app)
    
---
## 컨벤션
### 디렉토리 구조
- 스칼라의 기본 컨벤션을 따른다.
- src/main/scala 하위에 Main.scala가 위치힌다.
### 프로젝트 이름
- 영어 소문자로 표기한다.
- 띄어쓰기는 `-`로 표기한다.

예시) working-scala
### 파일 이름
- 파스칼 케이스로 표기한다.

예시) FileName.scala
### main 클래스 이름
- Main으로 통일한다.
