package forex.services.cache

import forex.domain.Rate
import io.circe._
import io.circe.generic.semiauto._


object Protocol {

  implicit val rateDecoder: Decoder[Rate] = deriveDecoder
  implicit val rateEncoder: Encoder[Rate] = deriveEncoder

}
