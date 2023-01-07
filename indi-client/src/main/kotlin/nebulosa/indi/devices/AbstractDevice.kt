package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.protocol.*

internal abstract class AbstractDevice(
    override val client: INDIClient,
    internal val handler: DeviceProtocolHandler,
    override val name: String,
    protected val properties: LinkedHashMap<String, PropertyVector<*, *>> = linkedMapOf(),
) : Device, Map<String, PropertyVector<*, *>> by properties {

    override var isConnected = false

    private fun handleVectorMessage(message: Vector<*>) {
        when (message) {
            is DefVector<*> -> {
                val property = when (message) {
                    is DefBLOBVector -> return
                    is DefLightVector -> return
                    is DefNumberVector -> return
                    is DefSwitchVector -> {
                        val properties = LinkedHashMap<String, SwitchProperty>()

                        for (e in message) {
                            val property = SwitchProperty(e.name, e.label, e.value == SwitchState.ON)
                            properties[property.name] = property
                        }

                        SwitchPropertyVector(
                            message.name, message.label, message.group,
                            message.perm, message.rule, message.state,
                            properties,
                        )
                    }
                    is DefTextVector -> return
                }

                properties[property.name] = property

                handler.fireOnEventReceived(DevicePropertyChanged(this, property))
            }
            is SetVector<*> -> return
            else -> return
        }

    }

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CONNECTION" -> {
                        val connected = message["CONNECT"]?.isOn() == true

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
            else -> Unit
        }

        if (message is Vector<*>) {
            handleVectorMessage(message)
        }
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        client.sendMessageToServer(message)
    }

    override fun connect() {
        if (!isConnected) {
            handler.fireOnEventReceived(DeviceIsConnecting(this))

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
