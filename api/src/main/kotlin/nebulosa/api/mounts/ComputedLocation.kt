package nebulosa.api.mounts

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.AzimuthSerializer
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.LSTSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.indi.device.mount.PierSide
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import java.time.LocalDateTime

data class ComputedLocation(
    @field:JsonSerialize(using = RightAscensionSerializer::class) var rightAscension: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) var declination: Angle = 0.0,
    @field:JsonSerialize(using = RightAscensionSerializer::class) var rightAscensionJ2000: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) var declinationJ2000: Angle = 0.0,
    @field:JsonSerialize(using = AzimuthSerializer::class) var azimuth: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) var altitude: Angle = 0.0,
    var constellation: Constellation = Constellation.AND,
    @field:JsonSerialize(using = LSTSerializer::class) var lst: Angle = 0.0,
    @field:JsonFormat(pattern = "HH:mm") var meridianAt: LocalDateTime = LocalDateTime.MIN,
    @field:JsonSerialize(using = LSTSerializer::class) var timeLeftToMeridianFlip: Angle = 0.0,
    var pierSide: PierSide = PierSide.NEITHER,
)
