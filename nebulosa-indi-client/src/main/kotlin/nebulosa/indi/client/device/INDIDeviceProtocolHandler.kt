package nebulosa.indi.client.device

import nebulosa.indi.device.AbstractINDIDeviceProvider
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.DefTextVector
import nebulosa.indi.protocol.DelProperty
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Message
import nebulosa.indi.protocol.parser.CloseConnectionListener
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.util.concurrent.LinkedBlockingQueue

abstract class INDIDeviceProtocolHandler : AbstractINDIDeviceProvider(), MessageSender, INDIProtocolParser, CloseConnectionListener {

    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    @Volatile private var protocolReader: INDIProtocolReader? = null
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)

    override val isClosed
        get() = protocolReader == null || !protocolReader!!.isRunning

    protected abstract fun newCamera(message: INDIProtocol, executable: String): Camera

    protected abstract fun newMount(message: INDIProtocol, executable: String): Mount

    protected abstract fun newFocuser(message: INDIProtocol): Focuser

    protected abstract fun newFilterWheel(message: INDIProtocol): FilterWheel

    protected abstract fun newRotator(message: INDIProtocol): Rotator

    protected abstract fun newGPS(message: INDIProtocol): GPS

    open fun start() {
        if (protocolReader == null) {
            protocolReader = INDIProtocolReader(this, Thread.MIN_PRIORITY)
            protocolReader!!.registerCloseConnectionListener(this)
            protocolReader!!.start()
        }
    }

    override fun close() {
        if (protocolReader == null) return

        try {
            protocolReader!!.close()
        } finally {
            protocolReader = null

            super.close()

            notRegisteredDevices.clear()
            messageQueueCounter.clear()
            messageReorderingQueue.clear()
        }
    }

    private fun takeMessageFromReorderingQueue(device: Device) {
        if (messageReorderingQueue.isNotEmpty()) {
            repeat(messageReorderingQueue.size) {
                val queuedMessage = messageReorderingQueue.take()

                if (queuedMessage.device == device.name) {
                    handleMessage(queuedMessage)
                } else {
                    messageReorderingQueue.offer(queuedMessage)
                }
            }
        }
    }

    @Synchronized
    override fun handleMessage(message: INDIProtocol) {
        if (message.device in notRegisteredDevices) return

        if (message is DefTextVector) {
            when (message.name) {
                "DRIVER_INFO" -> {
                    val interfaceType = message["DRIVER_INTERFACE"]?.value?.toIntOrNull() ?: 0
                    val executable = message["DRIVER_EXEC"]?.value ?: ""
                    var registered = false

                    if (DeviceInterfaceType.isCamera(interfaceType)) {
                        registered = true

                        with(newCamera(message, executable)) {
                            if (registerCamera(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (DeviceInterfaceType.isMount(interfaceType)) {
                        registered = true

                        with(newMount(message, executable)) {
                            if (registerMount(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (DeviceInterfaceType.isFilterWheel(interfaceType)) {
                        registered = true

                        with(newFilterWheel(message)) {
                            if (registerFilterWheel(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (DeviceInterfaceType.isFocuser(interfaceType)) {
                        registered = true

                        with(newFocuser(message)) {
                            if (registerFocuser(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (DeviceInterfaceType.isRotator(interfaceType)) {
                        registered = true

                        with(newRotator(message)) {
                            if (registerRotator(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (DeviceInterfaceType.isGPS(interfaceType)) {
                        registered = true

                        with(newGPS(message)) {
                            if (registerGPS(this)) {
                                handleMessage(message)
                                takeMessageFromReorderingQueue(this)
                            }
                        }
                    }

                    if (!registered) {
                        LOG.warn("device is not registered. name={}, interface={}, executable={}", message.device, interfaceType, executable)
                        notRegisteredDevices.add(message.device)
                    }

                    return
                }
            }
        }

        when (message) {
            is Message -> {
                val device = device(message.device)

                if (device == null) {
                    val text = "[%s]: %s\n".format(message.timestamp, message.message)
                    fireOnEventReceived(DeviceMessageReceived(null, text))
                } else if (message.name.isNotEmpty()) {
                    device.handleMessage(message)
                }

                LOG.debug { "message received: $message" }

                return
            }
            is DelProperty -> {
                if (message.name.isEmpty() && message.device.isNotEmpty()) {
                    val device = device(message.device)

                    device?.close()

                    when (device) {
                        is Camera -> unregisterCamera(device)
                        is Mount -> unregisterMount(device)
                        is FilterWheel -> unregisterFilterWheel(device)
                        is Focuser -> unregisterFocuser(device)
                        is Rotator -> unregisterRotator(device)
                        is GPS -> unregisterGPS(device)
                    }

                    return
                }
            }
            else -> Unit
        }

        if (message.device.isEmpty() || message.device in notRegisteredDevices) {
            messageReorderingQueue.remove(message)
            return
        }

        val device = device(message.device)

        if (device != null) {
            device.handleMessage(message)

            messageReorderingQueue.remove(message)

            LOG.debug { "message received: $message" }
        } else {
            if (message in messageQueueCounter) {
                val counter = messageQueueCounter[message]!!

                if (counter < 2048) {
                    messageQueueCounter[message] = counter + 1
                    messageReorderingQueue.offer(message)
                } else {
                    messageReorderingQueue.remove(message)
                    LOG.warn("message looping detected: $message")
                }
            } else {
                messageQueueCounter[message] = 1
                messageReorderingQueue.offer(message)
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<INDIDeviceProtocolHandler>()
    }
}
