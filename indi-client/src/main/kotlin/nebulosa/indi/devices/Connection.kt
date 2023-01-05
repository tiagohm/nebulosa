package nebulosa.indi.devices

data class Connection(
    val mode: ConnectionMode,
    val serialPort: String = "",
    val serialBaudRate: Int = 9600,
) {

    companion object {

        @JvmStatic val NONE = Connection(ConnectionMode.NONE)
        @JvmStatic val SERIAL_BAUD_RATES = listOf(9600, 19200, 38400, 57600, 115200, 230400)
    }
}
