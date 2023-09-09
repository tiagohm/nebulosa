package nebulosa.indi.device.filterwheel

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.Device
import nebulosa.json.HasJson

interface FilterWheel : Device, HasJson {

    val count: Int

    val position: Int

    val moving: Boolean

    fun moveTo(position: Int)

    fun syncNames(names: Iterable<String>)

    override fun writeToJson(gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", name)
        gen.writeBooleanField("connected", connected)
        gen.writeNumberField("count", count)
        gen.writeNumberField("position", position)
        gen.writeBooleanField("moving", moving)
        gen.writeEndObject()
    }

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_apogee_wheel",
            "indi_asi_wheel",
            "indi_atik_wheel",
            "indi_fli_wheel",
            "indi_manual_wheel",
            "indi_optec_wheel",
            "indi_qhycfw1_wheel",
            "indi_qhycfw2_wheel",
            "indi_qhycfw3_wheel",
            "indi_quantum_wheel",
            "indi_simulator_wheel",
            "indi_sx_wheel",
            "indi_trutech_wheel",
            "indi_xagyl_wheel",
        )
    }
}
