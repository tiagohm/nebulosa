package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.events.DeviceConnectedEvent
import nebulosa.indi.devices.events.DeviceDisconnectedEvent
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.parser.INDIProtocolHandler

abstract class Device(
    val client: INDIClient,
    val handler: DeviceProtocolHandler,
    val name: String,
) : INDIProtocolHandler {

    @Volatile private var isConnected = false

    override fun handleMessage(message: INDIProtocol) {
        if (message is SwitchVector<*>) {
            if (message.name == "CONNECTION") {
                val connected = message.firstOnSwitch().name == "CONNECT"

                if (connected) {
                    handler.fireOnEventReceived(this, DeviceConnectedEvent(this))
                } else if (isConnected) {
                    handler.fireOnEventReceived(this, DeviceDisconnectedEvent(this))
                }

                isConnected = connected
            }
        }
    }

    fun connect() {
        sendNewSwitch("CONNECTION", "CONNECT" to true)
    }

    fun disconnect() {
        sendNewSwitch("CONNECTION", "DISCONNECT" to true)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sendMessageToServer(message: INDIProtocol) = client.sendMessageToServer(message)

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
