package forex.http

/*
This code is original from Gabriel's project: gvolpe/pfps-shopping-cart
Please refer to the following link.
https://github.com/gvolpe/pfps-shopping-cart/blob/second-edition/modules/tests/src/main/scala/suite/HttpSuite.scala
 */

import scala.util.control.NoStackTrace

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import weaver.scalacheck.Checkers
import weaver.{Expectations, SimpleIOSuite}


trait HttpSuite extends SimpleIOSuite with Checkers {

  case object DummyError extends NoStackTrace

  def expectHttpBodyAndStatus[A: Encoder](
    routes: HttpRoutes[IO],
    req: Request[IO]
  )(
    expectedBody: A,
    expectedStatus: Status
  ): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.asJson.map { json =>
          expect.same(resp.status, expectedStatus) |+|
            expect.same(json.dropNullValues, expectedBody.asJson.dropNullValues)
        }
      case None => IO.pure(failure("route not found"))
    }

  def expectHttpStatus(routes: HttpRoutes[IO], req: Request[IO])(expectedStatus: Status): IO[Expectations] =
    routes.run(req).value.map {
      case Some(resp) => expect.same(resp.status, expectedStatus)
      case None => failure("route not found")
    }
}
