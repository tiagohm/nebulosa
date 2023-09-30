package nebulosa.api.mounts

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.mount.Mount
import nebulosa.json.modules.ToJson
import nebulosa.math.AngleFormatter
import nebulosa.math.format
import nebulosa.math.toDegrees
import nebulosa.math.toMeters
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class MountConverter : ToJson<Mount> {

    override val type = Mount::class.java

    override fun serialize(value: Mount, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeBooleanField("slewing", value.slewing)
        gen.writeBooleanField("tracking", value.tracking)
        gen.writeBooleanField("canAbort", value.canAbort)
        gen.writeBooleanField("canSync", value.canSync)
        gen.writeBooleanField("canGoTo", value.canGoTo)
        gen.writeBooleanField("canHome", value.canHome)
        gen.writeObjectField("slewRates", value.slewRates)
        gen.writeObjectField("slewRate", value.slewRate)
        gen.writeStringField("mountType", value.mountType.name)
        gen.writeObjectField("trackModes", value.trackModes)
        gen.writeStringField("trackMode", value.trackMode.name)
        gen.writeStringField("pierSide", value.pierSide.name)
        gen.writeNumberField("guideRateWE", value.guideRateWE)
        gen.writeNumberField("guideRateNS", value.guideRateNS)
        gen.writeStringField("rightAscension", value.rightAscension.format(AngleFormatter.HMS))
        gen.writeStringField("declination", value.declination.format(AngleFormatter.SIGNED_DMS))
        gen.writeBooleanField("canPulseGuide", value.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", value.pulseGuiding)
        gen.writeBooleanField("canPark", value.canPark)
        gen.writeBooleanField("parking", value.parking)
        gen.writeBooleanField("parked", value.parked)
        gen.writeBooleanField("hasGPS", value.hasGPS)
        gen.writeNumberField("longitude", value.longitude.toDegrees)
        gen.writeNumberField("latitude", value.latitude.toDegrees)
        gen.writeNumberField("elevation", value.elevation.toMeters)
        gen.writeNumberField("dateTime", value.dateTime.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli())
        gen.writeNumberField("offsetInMinutes", value.dateTime.offset.totalSeconds / 60)
        gen.writeEndObject()
    }
}
