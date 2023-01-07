package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.protocol.*

internal abstract class AbstractDevice(
    override val client: INDIClient,
    internal val handler: DeviceProtocolHandler,
    override val name: String,
) : Device {

    override var isConnected = false
    override var isConnecting = false
    override var connectionMode = ConnectionMode.NONE

    @Volatile @JvmField var devicePort = ""
    @Volatile @JvmField var deviceBaudRate = 9600

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CONNECTION" -> {
                        val connected = message["CONNECT"]?.isOn() == true

                        isConnecting = false

                        if (connected != isConnected) {
                            if (connected) {
                                isConnected = true

                                handler.fireOnEventReceived(DeviceConnected(this))
                            } else if (isConnected) {
                                isConnected = false

                                handler.fireOnEventReceived(DeviceDisconnected(this))
                            }
                        }
                    }
                    "CONNECTION_MODE" -> {
                        connectionMode = message
                            .firstOnSwitchOrNull()?.name?.replace("CONNECTION_", "")
                            ?.let(ConnectionMode::valueOf)
                            ?: ConnectionMode.NONE

                        handler.fireOnEventReceived(DeviceConnectionModeChanged(this))
                    }
                    "DEVICE_BAUD_RATE" -> {
                        deviceBaudRate = message.firstOnSwitchOrNull()?.name?.toIntOrNull() ?: 9600
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "DEVICE_PORT" -> {
                        devicePort = message["PORT"]?.value ?: ""
                    }
                }
            }
            else -> Unit
        }
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        client.sendMessageToServer(message)
    }

    override fun connect(connection: Connection) {
        if (!isConnected) {
            isConnecting = true

            handler.fireOnEventReceived(DeviceIsConnecting(this))

            if (connection != Connection.NONE) {
                sendNewSwitch("CONNECTION_MODE", "CONNECTION_${connection.mode}" to true)
            }

            if (connection.mode == ConnectionMode.SERIAL) {
                require(connection.serialPort.isNotBlank()) { "invalid serial port: ${connection.serialPort}" }
                require(connection.serialBaudRate in Connection.SERIAL_BAUD_RATES) { "invalid serial baud rate: ${connection.serialBaudRate}" }
                sendNewText("DEVICE_PORT", "PORT" to connection.serialPort)
                sendNewSwitch("DEVICE_BAUD_RATE", "${connection.serialBaudRate}" to true)
            }

            sendNewSwitch("CONNECTION", "CONNECT" to true)
        }
    }

    override fun disconnect() {
        sendNewSwitch("CONNECTION", "DISCONNECT" to true)
    }

    protected fun sendNewSwitch(
        name: String,
        vararg elements: Pair<String, Boolean>,
    ) {
        val vector = NewSwitchVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneSwitch()
            switch.name = element.first
            switch.value = if (element.second) SwitchState.ON else SwitchState.OFF
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    protected fun sendNewNumber(
        name: String,
        vararg elements: Pair<String, Double>,
    ) {
        val vector = NewNumberVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneNumber()
            switch.name = element.first
            switch.value = element.second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }

    protected fun sendNewText(
        name: String,
        vararg elements: Pair<String, String>,
    ) {
        val vector = NewTextVector()
        vector.device = this.name
        vector.name = name

        for (element in elements) {
            val switch = OneText()
            switch.name = element.first
            switch.value = element.second
            vector.elements.add(switch)
        }

        sendMessageToServer(vector)
    }
}
