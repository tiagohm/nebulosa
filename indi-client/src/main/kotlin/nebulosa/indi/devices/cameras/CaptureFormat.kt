package nebulosa.indi.devices.cameras

data class CaptureFormat(val name: String, val label: String) {

    override fun toString() = label
}
