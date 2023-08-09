package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.SlewRate
import nebulosa.math.AngleFormatter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
@Qualifier("serializer")
class MountSerializer : StdSerializer<Mount>(Mount::class.java) {

    override fun serialize(
        mount: Mount,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("name", mount.name)
        gen.writeBooleanField("connected", mount.connected)
        gen.writeBooleanField("slewing", mount.slewing)
        gen.writeBooleanField("tracking", mount.tracking)
        gen.writeBooleanField("canAbort", mount.canAbort)
        gen.writeBooleanField("canSync", mount.canSync)
        gen.writeBooleanField("canGoTo", mount.canGoTo)
        gen.writeBooleanField("canHome", mount.canHome)
        gen.writeArrayFieldStart("slewRates")
        mount.slewRates.forEach { gen.writeSlewRate(it) }
        gen.writeEndArray()
        mount.slewRate?.also { gen.writeFieldName("slewRate"); gen.writeSlewRate(it) }
        gen.writeStringField("mountType", mount.mountType.name)
        gen.writeArrayFieldStart("trackModes")
        mount.trackModes.forEach { gen.writeString(it.name) }
        gen.writeEndArray()
        gen.writeStringField("trackMode", mount.trackMode.name)
        gen.writeStringField("pierSide", mount.pierSide.name)
        gen.writeNumberField("guideRateWE", mount.guideRateWE)
        gen.writeNumberField("guideRateNS", mount.guideRateNS)
        gen.writeStringField("rightAscension", mount.rightAscension.format(AngleFormatter.HMS))
        gen.writeStringField("declination", mount.declination.format(AngleFormatter.SIGNED_DMS))
        gen.writeBooleanField("canPulseGuide", mount.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", mount.pulseGuiding)
        gen.writeBooleanField("canPark", mount.canPark)
        gen.writeBooleanField("parking", mount.parking)
        gen.writeBooleanField("parked", mount.parked)
        gen.writeBooleanField("hasGPS", mount.hasGPS)
        gen.writeNumberField("longitude", mount.longitude.degrees)
        gen.writeNumberField("latitude", mount.latitude.degrees)
        gen.writeNumberField("elevation", mount.elevation.meters)
        gen.writeNumberField("dateTime", mount.dateTime.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli())
        gen.writeNumberField("offsetInMinutes", mount.dateTime.offset.totalSeconds / 60)
        gen.writeEndObject()
    }

    companion object {

        @JvmStatic
        private fun JsonGenerator.writeSlewRate(slewRate: SlewRate) {
            writeStartObject()
            writeStringField("name", slewRate.name)
            writeStringField("label", slewRate.label)
            writeEndObject()
        }
    }
}
