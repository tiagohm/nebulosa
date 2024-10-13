package nebulosa.api.mounts

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.converters.angle.AzimuthSerializer
import nebulosa.api.converters.angle.DeclinationSerializer
import nebulosa.api.converters.angle.LSTSerializer
import nebulosa.api.converters.angle.RightAscensionSerializer
import nebulosa.indi.device.mount.PierSide
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import java.time.LocalDateTime

data class ComputedLocation(
    @field:JsonSerialize(using = RightAscensionSerializer::class) @JvmField var rightAscension: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) @JvmField var declination: Angle = 0.0,
    @field:JsonSerialize(using = RightAscensionSerializer::class) @JvmField var rightAscensionJ2000: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) @JvmField var declinationJ2000: Angle = 0.0,
    @field:JsonSerialize(using = AzimuthSerializer::class) @JvmField var azimuth: Angle = 0.0,
    @field:JsonSerialize(using = DeclinationSerializer::class) @JvmField var altitude: Angle = 0.0,
    @JvmField var constellation: Constellation = Constellation.AND,
    @field:JsonSerialize(using = LSTSerializer::class) @JvmField var lst: Angle = 0.0,
    @field:JsonFormat(pattern = "HH:mm") @JvmField var meridianAt: LocalDateTime = LocalDateTime.MIN,
    @field:JsonSerialize(using = LSTSerializer::class) @JvmField var timeLeftToMeridianFlip: Angle = 0.0,
    @JvmField var pierSide: PierSide = PierSide.NEITHER,
)
