package nebulosa.indi.devices.mounts

data class SlewRate(
    val name: String,
    val label: String,
) {

    override fun toString() = label
}
