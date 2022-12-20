package forex.services.cache

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Option[Rate]]

  def set(pair: Rate.Pair, rate: Rate): F[Boolean]
}