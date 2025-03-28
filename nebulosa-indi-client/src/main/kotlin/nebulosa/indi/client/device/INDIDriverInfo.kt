package nebulosa.indi.client.device

import nebulosa.indi.device.DriverInfo
import nebulosa.indi.protocol.TextVector

data class INDIDriverInfo(
    override val name: String,
    @JvmField val executable: String,
    override val version: String,
    @JvmField val interfaceType: Int,
) : DriverInfo {

    companion object {

        @JvmStatic
        fun from(message: TextVector<*>): INDIDriverInfo? {
            return INDIDriverInfo(
                message.device,
                message["DRIVER_EXEC"]?.value?.takeIf { it.isNotBlank() } ?: return null,
                message["DRIVER_VERSION"]?.value?.takeIf { it.isNotBlank() } ?: return null,
                message["DRIVER_INTERFACE"]?.value?.toIntOrNull() ?: return null,
            )
        }
    }
}
