package forex.http

import cats.effect.Sync
import forex.programs.RatesProgram
import org.http4s.dsl.Http4sDsl

class HealthRoute[F[_]: Sync] extends Http4sDsl[F]{

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap(Sync[F].fromEither).flatMap { rate =>
        Ok(rate.asGetApiResponse)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
