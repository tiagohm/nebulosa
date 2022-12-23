package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.parser.INDIProtocolHandler

abstract class Device(
    val client: INDIClient,
    val handler: DeviceProtocolHandler,
    val name: String,
) : LinkedHashMap<String, Any?>(), INDIProtocolHandler {

    @Volatile @JvmField var isConnected = false

    override fun handleMessage(message: INDIProtocol) {
        if (message is SwitchVector<*>) {
            if (message.name == "CONNECTION") {
                val connected = message.firstOnSwitch().name == "CONNECT"

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
        }
    }

    fun connect() {
        sendNewSwitch("CONNECTION", "CONNECT" to true)
    }

    fun disconnect() {
        sendNewSwitch("CONNECTION", "DISCONNECT" to true)
    }

    fun sendMessageToServer(message: INDIProtocol) = client.sendMessageToServer(message)

    fun enableBlob() = sendMessageToServer(EnableBLOB().also { it.device = name })

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
