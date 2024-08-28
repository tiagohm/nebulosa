package nebulosa.api.dustcap

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.dustcap.DustCap
import org.springframework.stereotype.Component

@Component
class DustCapSerializer : DeviceSerializer<DustCap>(DustCap::class.java) {

    override fun JsonGenerator.serialize(value: DustCap) {
        writeBooleanField("canPark", value.canPark)
        writeBooleanField("parking", value.parking)
        writeBooleanField("parked", value.parked)
    }
}
