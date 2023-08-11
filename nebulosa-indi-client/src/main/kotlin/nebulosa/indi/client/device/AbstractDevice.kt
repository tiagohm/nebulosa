package nebulosa.indi.client.device

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

internal abstract class AbstractDevice(
    override val sender: MessageSender,
    internal val handler: DeviceProtocolHandler,
    override val name: String,
) : Device {

    override val properties = linkedMapOf<String, PropertyVector<*, *>>()

    override val messages = LinkedList<String>()

    override var connected = false
        protected set

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CONNECTION" -> {
                        val connected = message["CONNECT"]?.value ?: false

                        if (connected != this.connected) {
                            if (connected) {
                                this.connected = true

                                handler.fireOnEventReceived(DeviceConnected(this))

                                ask()
                            } else if (this.connected) {
                                this.connected = false

                                handler.fireOnEventReceived(DeviceDisconnected(this))
                            }
                        } else if (!connected && message.state == PropertyState.ALERT) {
                            handler.fireOnEventReceived(DeviceConnectionFailed(this))
                        }
                    }
                }
            }
            is DelProperty -> {
                val property = properties.remove(message.name) ?: return
                handler.fireOnEventReceived(DevicePropertyDeleted(property))
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

                handler.fireOnEventReceived(DevicePropertyChanged(property))
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

                handler.fireOnEventReceived(DevicePropertyChanged(property))
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

    companion object {

        @JvmStatic private val LOG = loggerFor<AbstractDevice>()

        @JvmStatic
        fun <T : Device> Class<out T>.create(sender: MessageSender, handler: DeviceProtocolHandler, name: String): T {
            return getConstructor(MessageSender::class.java, DeviceProtocolHandler::class.java, String::class.java)
                .newInstance(sender, handler, name)
        }
    }
}
