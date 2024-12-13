package nebulosa.alpaca.indi.device

import nebulosa.indi.device.DriverInfo

data class ASCOMDriverInfo(override val name: String, override val version: String) : DriverInfo
