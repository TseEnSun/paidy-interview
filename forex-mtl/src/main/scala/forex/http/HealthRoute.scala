package forex.http

import cats.effect.Sync
import forex.services.HealthCheckService
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.Router

class HealthRoute[F[_]: Sync](healthCheck: HealthCheckService[F]) extends Http4sDsl[F]{

  private[http] val prefixPath = "/health_check"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root  =>
      Ok(healthCheck.status)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
