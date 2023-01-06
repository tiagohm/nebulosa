package nebulosa.desktop.core.util

import javafx.util.StringConverter
import nebulosa.indi.devices.Device

open class DeviceStringConverter<D : Device> : StringConverter<D>() {

    override fun toString(device: D?) = device?.name ?: "-"

    override fun fromString(text: String?) = null
}
