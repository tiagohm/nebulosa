package nebulosa.indi.client.device

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.camera.CameraDevice
import nebulosa.indi.device.*
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.dome.Dome
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.Vector
import nebulosa.log.loggerFor
import java.util.*

internal abstract class INDIDevice : Device {

    abstract override val sender: INDIClient

    override val properties = linkedMapOf<String, PropertyVector<*, *>>()
    override val messages = LinkedList<String>()

    override val id = UUID.randomUUID().toString()

    @Volatile override var connected = false
        protected set

    private fun addMessageAndFireEvent(text: String) {
        synchronized(messages) {
            messages.addFirst(text)

            sender.fireOnEventReceived(DeviceMessageReceived(this, text))

            if (messages.size > 100) {
                messages.removeLast()
            }
        }
    }

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CONNECTION" -> {
                        val connected = message["CONNECT"]?.value ?: false

                        if (connected != this.connected) {
                            if (connected) {
                                this.connected = true

                                sender.fireOnEventReceived(DeviceConnected(this))

                                ask()
                            } else if (this.connected) {
                                this.connected = false

                                sender.fireOnEventReceived(DeviceDisconnected(this))
                            }
                        } else if (!connected && message.state == PropertyState.ALERT) {
                            sender.fireOnEventReceived(DeviceConnectionFailed(this))
                        }
                    }
                }
            }
            is DelProperty -> {
                val property = properties.remove(message.name) ?: return
                sender.fireOnEventReceived(DevicePropertyDeleted(property))
            }
            is Message -> {
                addMessageAndFireEvent("[%s]: %s".format(message.timestamp, message.message))
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
                            this,
                            message.name, message.label, message.group,
                            message.perm, message.state,
                            properties,
                        )
                    }
                    is DefSwitchVector -> {
                        val properties = LinkedHashMap<String, SwitchProperty>()

                        for (e in message) {
                            val property = SwitchProperty(e.name, e.label, e.value)
                            properties[property.name] = property
                        }

                        SwitchPropertyVector(
                            this,
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
                            this,
                            message.name, message.label, message.group,
                            message.perm, message.state,
                            properties,
                        )
                    }
                    else -> return
                }

                properties[property.name] = property

                sender.fireOnEventReceived(DevicePropertyChanged(property))
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
                            property.value = e.value
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

                sender.fireOnEventReceived(DevicePropertyChanged(property))
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
                is GPS -> "ACTIVE_GPS"
                else -> return@mapNotNull null
            } to it.name
        }

        // TODO:ACTIVE_SKYQUALITY, ACTIVE_WEATHER

        LOG.info("$name is snooping the devices: $message")

        sendNewText("ACTIVE_DEVICES", message)
    }

    override fun connect() {
        if (!connected) {
            sendNewSwitch("CONNECTION", "CONNECT" to true)
        }
    }

    override fun disconnect() {
        sendNewSwitch("CONNECTION", "DISCONNECT" to true)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CameraDevice) return false

        if (sender != other.sender) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<INDIDevice>()
    }
}
