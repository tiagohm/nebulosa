package nebulosa.indi.client.device

import nebulosa.indi.device.AbstractINDIDeviceProvider
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.DelProperty
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Message
import nebulosa.indi.protocol.TextVector
import nebulosa.indi.protocol.parser.CloseConnectionListener
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.concurrent.LinkedBlockingQueue

abstract class INDIDeviceProtocolHandler : AbstractINDIDeviceProvider(), MessageSender, INDIProtocolParser, CloseConnectionListener {

    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    @Volatile private var protocolReader: INDIProtocolReader? = null
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)

    override val isClosed
        get() = protocolReader == null || !protocolReader!!.isRunning

    protected abstract fun newCamera(driver: INDIDriverInfo): Camera

    protected abstract fun newMount(driver: INDIDriverInfo): Mount

    protected abstract fun newFocuser(driver: INDIDriverInfo): Focuser

    protected abstract fun newFilterWheel(driver: INDIDriverInfo): FilterWheel

    protected abstract fun newRotator(driver: INDIDriverInfo): Rotator

    protected abstract fun newGPS(driver: INDIDriverInfo): GPS

    protected abstract fun newGuideOutput(driver: INDIDriverInfo): GuideOutput

    protected abstract fun newLightBox(driver: INDIDriverInfo): LightBox

    protected abstract fun newDustCap(driver: INDIDriverInfo): DustCap

    private fun registerCamera(driver: INDIDriverInfo): Camera? {
        return if (camera(driver.name) == null) {
            newCamera(driver).also(::registerCamera)
        } else {
            null
        }
    }

    private fun registerMount(driver: INDIDriverInfo): Mount? {
        return if (mount(driver.name) == null) {
            newMount(driver).also(::registerMount)
        } else {
            null
        }
    }

    private fun registerFocuser(driver: INDIDriverInfo): Focuser? {
        return if (focuser(driver.name) == null) {
            newFocuser(driver).also(::registerFocuser)
        } else {
            null
        }
    }

    private fun registerRotator(driver: INDIDriverInfo): Rotator? {
        return if (rotator(driver.name) == null) {
            newRotator(driver).also(::registerRotator)
        } else {
            null
        }
    }

    private fun registerFilterWheel(driver: INDIDriverInfo): FilterWheel? {
        return if (wheel(driver.name) == null) {
            newFilterWheel(driver).also(::registerFilterWheel)
        } else {
            null
        }
    }

    private fun registerGPS(driver: INDIDriverInfo): GPS? {
        return if (gps(driver.name) == null) {
            newGPS(driver).also(::registerGPS)
        } else {
            null
        }
    }

    private fun registerGuideOutput(driver: INDIDriverInfo): GuideOutput? {
        return if (guideOutput(driver.name) == null) {
            newGuideOutput(driver).also(::registerGuideOutput)
        } else {
            null
        }
    }

    private fun registerLightBox(driver: INDIDriverInfo): LightBox? {
        return if (lightBox(driver.name) == null) {
            newLightBox(driver).also(::registerLightBox)
        } else {
            null
        }
    }

    private fun registerDustCap(driver: INDIDriverInfo): DustCap? {
        return if (dustCap(driver.name) == null) {
            newDustCap(driver).also(::registerDustCap)
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
                    val driver = INDIDriverInfo.from(message) ?: return

                    val interfaceType = driver.interfaceType
                    var registered = false

                    if (DeviceInterfaceType.isCamera(interfaceType)) {
                        registered = true

                        registerCamera(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isMount(interfaceType)) {
                        registered = true

                        registerMount(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFilterWheel(interfaceType)) {
                        registered = true

                        registerFilterWheel(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFocuser(interfaceType)) {
                        registered = true

                        registerFocuser(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isRotator(interfaceType)) {
                        registered = true

                        registerRotator(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGPS(interfaceType)) {
                        registered = true

                        registerGPS(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGuider(interfaceType)) {
                        registered = true

                        registerGuideOutput(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isLightBox(interfaceType)) {
                        registered = true

                        registerLightBox(driver)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isDustCap(interfaceType)) {
                        registered = true

                        registerDustCap(driver)?.also {
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
                } else {
                    device.forEach { it.handleMessage(message) }
                }

                LOG.d { debug("message received: {}", message) }

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
                            is LightBox -> unregisterLightBox(device)
                            is DustCap -> unregisterDustCap(device)
                        }
                    }

                    LOG.d { debug("message received: {}", message) }

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

            LOG.d { debug("message received: {}", message) }
        } else {
            if (message in messageQueueCounter) {
                val counter = messageQueueCounter[message]!!

                if (counter < 2048) {
                    messageQueueCounter[message] = counter + 1
                    messageReorderingQueue.offer(message)
                } else {
                    messageReorderingQueue.remove(message)
                    LOG.warn("message looping detected: {}", message)
                }
            } else {
                messageQueueCounter[message] = 1
                messageReorderingQueue.offer(message)
            }
        }
    }

    companion object {

        private val LOG = loggerFor<INDIDeviceProtocolHandler>()
    }
}
