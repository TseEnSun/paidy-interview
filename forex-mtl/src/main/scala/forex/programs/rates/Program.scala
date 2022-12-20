package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services._

class Program[F[_]: Functor](
  ratesService: RatesService[F],
  cacheService: CacheService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value
    // TODO: Add rateCache get here
  }
}

object Program {

  def apply[F[_]: Functor](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
