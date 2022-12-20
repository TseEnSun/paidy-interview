package forex.domain

import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder


object HealthCheck {

  sealed trait Status
  object Status {
    case object OK extends Status
    case object Unreachable extends Status
  }

  case class RedisStatus(value: Status)

  case class AppStatus(redis: RedisStatus)

  implicit val statusEncoder: Encoder[Status] = Encoder.forProduct1("status")(_.toString)
  implicit val RedisStatusEncoder: Encoder[RedisStatus] = deriveUnwrappedEncoder[RedisStatus]
  implicit val appStatusEncoder: Encoder[AppStatus] = deriveEncoder
}
