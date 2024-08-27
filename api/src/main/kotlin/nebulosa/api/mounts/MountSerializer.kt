package nebulosa.api.mounts

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.mount.Mount
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.math.toDegrees
import nebulosa.math.toMeters
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class MountSerializer : DeviceSerializer<Mount>(Mount::class.java) {

    override fun JsonGenerator.serialize(value: Mount) {
        writeBooleanField("slewing", value.slewing)
        writeBooleanField("tracking", value.tracking)
        writeBooleanField("canAbort", value.canAbort)
        writeBooleanField("canSync", value.canSync)
        writeBooleanField("canGoTo", value.canGoTo)
        writeBooleanField("canHome", value.canHome)
        writeObjectField("slewRates", value.slewRates)
        writeObjectField("slewRate", value.slewRate)
        writeStringField("mountType", value.mountType.name)
        writeObjectField("trackModes", value.trackModes)
        writeStringField("trackMode", value.trackMode.name)
        writeStringField("pierSide", value.pierSide.name)
        writeNumberField("guideRateWE", value.guideRateWE)
        writeNumberField("guideRateNS", value.guideRateNS)
        writeStringField("rightAscension", value.rightAscension.formatHMS())
        writeStringField("declination", value.declination.formatSignedDMS())
        writeBooleanField("canPulseGuide", value.canPulseGuide)
        writeBooleanField("pulseGuiding", value.pulseGuiding)
        writeBooleanField("canPark", value.canPark)
        writeBooleanField("parking", value.parking)
        writeBooleanField("parked", value.parked)
        writeBooleanField("hasGPS", value.hasGPS)
        writeNumberField("longitude", value.longitude.toDegrees)
        writeNumberField("latitude", value.latitude.toDegrees)
        writeNumberField("elevation", value.elevation.toMeters)
        writeNumberField("dateTime", value.dateTime.toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli())
        writeNumberField("offsetInMinutes", value.dateTime.offset.totalSeconds / 60)
    }
}
