package nebulosa.indi.client.device

import nebulosa.indi.protocol.TextVector

data class DriverInfo(
    @JvmField val name: String,
    @JvmField val executable: String,
    @JvmField val version: String,
    @JvmField val interfaceType: Int,
) {

    companion object {

        @JvmStatic
        fun from(message: TextVector<*>): DriverInfo? {
            return DriverInfo(
                message.device,
                message["DRIVER_EXEC"]?.value?.takeIf { it.isNotBlank() } ?: return null,
                message["DRIVER_VERSION"]?.value?.takeIf { it.isNotBlank() } ?: return null,
                message["DRIVER_INTERFACE"]?.value?.toIntOrNull() ?: return null,
            )
        }
    }
}
