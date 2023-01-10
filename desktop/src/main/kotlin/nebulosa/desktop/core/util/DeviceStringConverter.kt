package nebulosa.desktop.core.util

import javafx.util.StringConverter
import nebulosa.indi.devices.Device

open class DeviceStringConverter<D : Device>(private val emptyText: String = "No device selected") : StringConverter<D>() {

    override fun toString(device: D?) = device?.name ?: emptyText

    override fun fromString(text: String?) = null
}
