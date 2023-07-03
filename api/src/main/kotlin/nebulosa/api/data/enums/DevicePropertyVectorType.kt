package nebulosa.api.data.enums

import nebulosa.indi.device.NumberPropertyVector
import nebulosa.indi.device.PropertyVector
import nebulosa.indi.device.SwitchPropertyVector
import nebulosa.indi.device.TextPropertyVector

enum class DevicePropertyVectorType {
    NUMBER,
    SWITCH,
    TEXT;

    companion object {

        @JvmStatic
        fun of(vector: PropertyVector<*, *>) = when (vector) {
            is NumberPropertyVector -> NUMBER
            is SwitchPropertyVector -> SWITCH
            is TextPropertyVector -> TEXT
        }
    }
}
