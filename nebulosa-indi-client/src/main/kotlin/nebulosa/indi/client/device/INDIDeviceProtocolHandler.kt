package nebulosa.indi.client.device

import nebulosa.indi.device.AbstractINDIDeviceProvider
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.DelProperty
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Message
import nebulosa.indi.protocol.TextVector
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

    protected abstract fun newCamera(name: String, executable: String): Camera

    protected abstract fun newMount(name: String, executable: String): Mount

    protected abstract fun newFocuser(name: String, executable: String): Focuser

    protected abstract fun newFilterWheel(name: String, executable: String): FilterWheel

    protected abstract fun newRotator(name: String, executable: String): Rotator

    protected abstract fun newGPS(name: String, executable: String): GPS

    protected abstract fun newGuideOutput(name: String, executable: String): GuideOutput

    private fun registerCamera(message: TextVector<*>): Camera? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && camera(message.name) == null) {
            newCamera(message.device, executable).also(::registerCamera)
        } else {
            null
        }
    }

    private fun registerMount(message: TextVector<*>): Mount? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && mount(message.name) == null) {
            newMount(message.device, executable).also(::registerMount)
        } else {
            null
        }
    }

    private fun registerFocuser(message: TextVector<*>): Focuser? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && focuser(message.name) == null) {
            newFocuser(message.device, executable).also(::registerFocuser)
        } else {
            null
        }
    }

    private fun registerRotator(message: TextVector<*>): Rotator? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && rotator(message.name) == null) {
            newRotator(message.device, executable).also(::registerRotator)
        } else {
            null
        }
    }

    private fun registerFilterWheel(message: TextVector<*>): FilterWheel? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && wheel(message.name) == null) {
            newFilterWheel(message.device, executable).also(::registerFilterWheel)
        } else {
            null
        }
    }

    private fun registerGPS(message: TextVector<*>): GPS? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && gps(message.name) == null) {
            newGPS(message.device, executable).also(::registerGPS)
        } else {
            null
        }
    }

    private fun registerGuideOutput(message: TextVector<*>): GuideOutput? {
        val executable = message["DRIVER_EXEC"]?.value

        return if (!executable.isNullOrEmpty() && message.device.isNotEmpty() && guideOutput(message.name) == null) {
            newGuideOutput(message.device, executable).also(::registerGuideOutput)
        } else {
            null
        }
    }

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

        if (message is TextVector<*>) {
            when (message.name) {
                "DRIVER_INFO" -> {
                    val interfaceType = message["DRIVER_INTERFACE"]?.value?.toIntOrNull() ?: 0
                    var registered = false

                    if (DeviceInterfaceType.isCamera(interfaceType)) {
                        registerCamera(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isMount(interfaceType)) {
                        registerMount(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFilterWheel(interfaceType)) {
                        registerFilterWheel(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFocuser(interfaceType)) {
                        registerFocuser(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isRotator(interfaceType)) {
                        registerRotator(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGPS(interfaceType)) {
                        registerGPS(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGuider(interfaceType)) {
                        registerGuideOutput(message)?.also {
                            registered = true
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (!registered) {
                        LOG.warn("device is not registered. name={}, interface={}", message.device, interfaceType)
                        notRegisteredDevices.add(message.device)
                    }

                    return
                }
            }
        }

        when (message) {
            is Message -> {
                val device = device(message.device)

                if (device.isEmpty()) {
                    val text = "[%s]: %s\n".format(message.timestamp, message.message)
                    fireOnEventReceived(DeviceMessageReceived(null, text))
                } else if (message.name.isNotEmpty()) {
                    device.forEach { it.handleMessage(message) }
                }

                LOG.debug { "message received: $message" }

                return
            }
            is DelProperty -> {
                if (message.name.isEmpty() && message.device.isNotEmpty()) {
                    for (device in device(message.device)) {
                        device.close()

                        when (device) {
                            is Camera -> unregisterCamera(device)
                            is Mount -> unregisterMount(device)
                            is FilterWheel -> unregisterFilterWheel(device)
                            is Focuser -> unregisterFocuser(device)
                            is Rotator -> unregisterRotator(device)
                            is GPS -> unregisterGPS(device)
                            is GuideOutput -> unregisterGuideOutput(device)
                        }
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

        if (device.isNotEmpty()) {
            device.forEach { it.handleMessage(message) }

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
