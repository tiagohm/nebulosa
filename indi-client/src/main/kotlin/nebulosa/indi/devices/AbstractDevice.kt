package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.domes.Dome
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.focusers.Focuser
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.rotators.Rotator
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.Vector
import org.slf4j.LoggerFactory
import java.util.*

internal abstract class AbstractDevice(
    override val client: INDIClient,
    internal val handler: DeviceProtocolHandler,
    override val name: String,
) : Device {

    override val properties = linkedMapOf<String, PropertyVector<*, *>>()

    override val messages = LinkedList<String>()

    override var isConnected = false
        protected set

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
            is DelProperty -> {
                val property = properties.remove(message.name) ?: return
                handler.fireOnEventReceived(DevicePropertyDeleted(this, property))
            }
            is Message -> {
                val text = "[%s]: %s".format(message.timestamp, message.message)
                messages.addFirst(text)
                handler.fireOnEventReceived(DeviceMessageReceived(this, text))
            }
            else -> Unit
        }

        if (message is Vector<*>) {
            handleVectorMessage(message)
        }
    }

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

    override fun snoop(devices: Iterable<Device?>) {
        val message = devices.mapNotNull {
            when (it) {
                is Camera -> "ACTIVE_CCD"
                is Mount -> "ACTIVE_TELESCOPE"
                is Focuser -> "ACTIVE_FOCUSER"
                is FilterWheel -> "ACTIVE_FILTER"
                is Rotator -> "ACTIVE_ROTATOR"
                is Dome -> "ACTIVE_DOME"
                else -> return@mapNotNull null
            } to it.name
        }

        // TODO:ACTIVE_SKYQUALITY, ACTIVE_WEATHER

        LOG.info("$name is snooping the devices: $message")

        sendNewText("ACTIVE_DEVICES", message)
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

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AbstractDevice::class.java)
    }
}
