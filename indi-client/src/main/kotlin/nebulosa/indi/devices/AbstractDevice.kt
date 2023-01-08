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
                    is DefLightVector -> return
                    is DefNumberVector -> {
                        val properties = LinkedHashMap<String, NumberProperty>()

                        for (e in message) {
                            val property = NumberProperty(e.name, e.label, e.value)
                            properties[property.name] = property
                        }

                        NumberPropertyVector(
                            message.name, message.label, message.group,
                            message.perm, message.state,
                            properties,
                        )
                    }
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
                    is DefTextVector -> {
                        val properties = LinkedHashMap<String, TextProperty>()

                        for (e in message) {
                            val property = TextProperty(e.name, e.label, e.value)
                            properties[property.name] = property
                        }

                        TextPropertyVector(
                            message.name, message.label, message.group,
                            message.perm, message.state,
                            properties,
                        )
                    }
                    else -> return
                }

                properties[property.name] = property

                handler.fireOnEventReceived(DevicePropertyChanged(this, property))
            }
            is SetVector<*> -> {
                val property = when (message) {
                    is SetLightVector -> return
                    is SetNumberVector -> {
                        val vector = properties[message.name] as? NumberPropertyVector ?: return

                        vector.state = message.state

                        for (e in message) {
                            val property = vector[e.name] ?: continue
                            property.value = e.value
                        }

                        vector
                    }
                    is SetSwitchVector -> {
                        val vector = properties[message.name] as? SwitchPropertyVector ?: return

                        vector.state = message.state

                        for (e in message) {
                            val property = vector[e.name] ?: continue
                            property.value = e.value == SwitchState.ON
                        }

                        vector
                    }
                    is SetTextVector -> {
                        val vector = properties[message.name] as? TextPropertyVector ?: return

                        vector.state = message.state

                        for (e in message) {
                            val property = vector[e.name] ?: continue
                            property.value = e.value
                        }

                        vector
                    }
                    else -> return
                }

                handler.fireOnEventReceived(DevicePropertyChanged(this, property))
            }
            else -> return
        }

    }

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is DelProperty -> {
                val property = properties.remove(message.name) ?: return
                handler.fireOnEventReceived(DevicePropertyDeleted(this, property))
            }
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
}
