package forex.services.rates.interpreters

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect._
import forex.config.OneFrameConfig
import org.http4s._
import org.http4s.client._
import org.http4s.circe._
import forex.domain.Rate
import forex.services.rates.{Algebra, Protocol, errors}
import org.http4s.Method.GET


class OneFrameHttp[F[_]: Sync](
  cfg: OneFrameConfig,
  client: Client[F]
) extends Algebra[F] {

  import Protocol._

  implicit val oneFrameEntityDecoder: EntityDecoder[F, OneFrameResponse] = jsonOf[F, OneFrameResponse]

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = {
    Uri.fromString(s"${cfg.host}:${cfg.port}").liftTo[F]
      .flatMap { uri =>
        val uriWithPathAndQuery = uri
          .withPath("/rates")
          .withQueryParam("pair", pair.show)
          // .withMultiValueQueryParams
        val headers = Headers.of(
          Header("token", cfg.token)
        )
        val request = Request[F](
          method = GET,
          uri = uriWithPathAndQuery,
          headers = headers
        )
        client.run(request).use {
          case Status.Successful(resp) =>
            resp.asJsonDecode[OneFrameResponse].map(x => Right(x.rates.head.toRate))
          case resp =>
            resp.as[String]
              .map { b =>
                Left(
                  errors.Error.OneFrameLookupFailed(s"Failed with code: ${resp.status.code} and body $b")
                )
              }
          }
        }
    }

  override def getMany(
    pairs: NonEmptyList[Rate.Pair]): F[Either[errors.Error, NonEmptyList[Rate]]] = {
    Uri.fromString(s"${cfg.host}:${cfg.port}").liftTo[F]
      .flatMap { uri =>
        val uriWithPathAndQuery = uri
          .withPath("/rates")
          .withMultiValueQueryParams(Map("pair" -> pairs.map(_.show).toList))
        val headers = Headers.of(
          Header("token", cfg.token)
        )
        val request = Request[F](
          method = GET,
          uri = uriWithPathAndQuery,
          headers = headers
        )
        client.run(request).use {
          case Status.Successful(resp) =>
            resp.asJsonDecode[OneFrameResponse].map(x => Right(x.rates.map(_.toRate)))
          case resp =>
            resp.as[String]
              .map { b =>
                Left(
                  errors.Error.OneFrameLookupFailed(s"Failed with code: ${resp.status.code} and body $b")
                )
              }
        }
      }
  }
}
