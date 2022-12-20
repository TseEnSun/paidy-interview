package forex

import cats.effect.{Concurrent, Timer}
import forex.config.ApplicationConfig
import forex.resources.AppResources
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.services.healthcheck.{Interpreter => HealthCheckInterperter}
import forex.services.cache.{Interpreter => CacheInterpreter}
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: Concurrent: Timer](
  config: ApplicationConfig,
  resources: AppResources[F]
) {
  private val healthCheckService: HealthCheckService[F] = HealthCheckInterperter.make(resources.redis)

  //  private val ratesService: RatesService[F] = RatesServices.dummy[F]
  private val ratesService: RatesService[F] = RatesServices.http(config.oneFrame, resources.client)

  private val cacheService: CacheService[F] = CacheInterpreter.make(resources.redis, config.redis)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.httpServer.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
