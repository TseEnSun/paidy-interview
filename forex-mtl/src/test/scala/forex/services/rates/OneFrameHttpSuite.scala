package forex.services.rates

import cats.Show
import cats.data.Kleisli
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.client.Client
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers
import forex.Generators.oneFrameResponseGen
import forex.config.OneFrameConfig
import forex.domain.Currency.show
import forex.domain.{Currency, Price, Timestamp}
import forex.services.RatesServices
import forex.services.rates.Protocol.{ExchangeRate, OneFrameResponse}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredEncoder, deriveUnwrappedEncoder}
import io.circe.{Encoder, Json}
import org.http4s.circe.jsonEncoderOf


object OneFrameHttpSuite extends SimpleIOSuite with Checkers {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val currencyEncoder: Encoder[Currency] = Encoder.instance[Currency] { show.show _ andThen Json.fromString }
  implicit val timestampEncoder: Encoder[Timestamp] = deriveUnwrappedEncoder[Timestamp]
  implicit val priceEncoder: Encoder[Price] = deriveUnwrappedEncoder[Price]
  implicit val exchangeRateEncoder: Encoder[ExchangeRate] = deriveConfiguredEncoder[ExchangeRate]
  implicit val oneFrameResponseEncoder: Encoder[OneFrameResponse] = deriveUnwrappedEncoder[OneFrameResponse]
  implicit val oneFrameResponseJsonEncoder: EntityEncoder[IO, OneFrameResponse] =
    jsonEncoderOf[IO, OneFrameResponse]
  implicit val showOneFrameResponse: Show[OneFrameResponse] = Show.fromToString
  val config: OneFrameConfig = OneFrameConfig(
    "http://localhost",
    8080,
    "dummyToken"
  )

  def routes(mkResponse: IO[Response[IO]]): Kleisli[IO, Request[IO], Response[IO]] =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "rates" => mkResponse
      }
      .orNotFound

  test("Response Ok 200") {
    forall(oneFrameResponseGen) { response =>
      val client = Client.fromHttpApp(routes(Ok(response)))

      RatesServices.http[IO](config, client)
        .get(response.rates.head.toRate.pair)
        .map {
          case Right(rate) =>
            expect.same(response.rates.head.toRate, rate)
          case _ =>
            failure("Response Ok 200 failed")
        }
    }
  }
}
