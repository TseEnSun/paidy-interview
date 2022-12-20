package forex.services.cache

import cats._
import cats.implicits._
import io.circe.syntax._
import io.circe.parser.decode
import forex.config.RedisConfig
import forex.domain.Rate
import dev.profunktor.redis4cats.RedisCommands


object Interpreter {
  def make[F[_]: Monad](
    redis: RedisCommands[F, String, String],
    config: RedisConfig
  ): Algebra[F] = {

    import Protocol._

    new Algebra[F] {
      override def get(pair: Rate.Pair): F[Option[Rate]] =
        redis.get(pair.show).map(_.map(decode[Rate]))

      override def set(pair: Rate.Pair, rate: Rate): F[Boolean] =
        redis.set(pair.show, rate.asJson.noSpaces) *>
          redis.expire(pair.show, config.expiredTime)
    }
  }
}
