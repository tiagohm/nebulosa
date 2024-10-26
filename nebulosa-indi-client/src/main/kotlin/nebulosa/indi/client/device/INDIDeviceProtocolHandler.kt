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
import nebulosa.log.w
import java.util.concurrent.LinkedBlockingQueue

abstract class INDIDeviceProtocolHandler : AbstractINDIDeviceProvider(), MessageSender, INDIProtocolParser, CloseConnectionListener {

    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    @Volatile private var protocolReader: INDIProtocolReader? = null
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)

    override val isClosed
        get() = protocolReader == null || !protocolReader!!.isRunning

    protected abstract fun newCamera(driverInfo: DriverInfo): Camera

    protected abstract fun newMount(driverInfo: DriverInfo): Mount

    protected abstract fun newFocuser(driverInfo: DriverInfo): Focuser

    protected abstract fun newFilterWheel(driverInfo: DriverInfo): FilterWheel

    protected abstract fun newRotator(driverInfo: DriverInfo): Rotator

    protected abstract fun newGPS(driverInfo: DriverInfo): GPS

    protected abstract fun newGuideOutput(driverInfo: DriverInfo): GuideOutput

    protected abstract fun newLightBox(driverInfo: DriverInfo): LightBox

    protected abstract fun newDustCap(driverInfo: DriverInfo): DustCap

    private fun registerCamera(driverInfo: DriverInfo): Camera? {
        return if (camera(driverInfo.name) == null) {
            newCamera(driverInfo).also(::registerCamera)
        } else {
            null
        }
    }

    private fun registerMount(driverInfo: DriverInfo): Mount? {
        return if (mount(driverInfo.name) == null) {
            newMount(driverInfo).also(::registerMount)
        } else {
            null
        }
    }

    private fun registerFocuser(driverInfo: DriverInfo): Focuser? {
        return if (focuser(driverInfo.name) == null) {
            newFocuser(driverInfo).also(::registerFocuser)
        } else {
            null
        }
    }

    private fun registerRotator(driverInfo: DriverInfo): Rotator? {
        return if (rotator(driverInfo.name) == null) {
            newRotator(driverInfo).also(::registerRotator)
        } else {
            null
        }
    }

    private fun registerFilterWheel(driverInfo: DriverInfo): FilterWheel? {
        return if (wheel(driverInfo.name) == null) {
            newFilterWheel(driverInfo).also(::registerFilterWheel)
        } else {
            null
        }
    }

    private fun registerGPS(driverInfo: DriverInfo): GPS? {
        return if (gps(driverInfo.name) == null) {
            newGPS(driverInfo).also(::registerGPS)
        } else {
            null
        }
    }

    private fun registerGuideOutput(driverInfo: DriverInfo): GuideOutput? {
        return if (guideOutput(driverInfo.name) == null) {
            newGuideOutput(driverInfo).also(::registerGuideOutput)
        } else {
            null
        }
    }

    private fun registerLightBox(driverInfo: DriverInfo): LightBox? {
        return if (lightBox(driverInfo.name) == null) {
            newLightBox(driverInfo).also(::registerLightBox)
        } else {
            null
        }
    }

    private fun registerDustCap(driverInfo: DriverInfo): DustCap? {
        return if (dustCap(driverInfo.name) == null) {
            newDustCap(driverInfo).also(::registerDustCap)
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
                    val driverInfo = DriverInfo.from(message) ?: return

                    val interfaceType = driverInfo.interfaceType
                    var registered = false

                    if (DeviceInterfaceType.isCamera(interfaceType)) {
                        registered = true

                        registerCamera(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isMount(interfaceType)) {
                        registered = true

                        registerMount(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFilterWheel(interfaceType)) {
                        registered = true

                        registerFilterWheel(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isFocuser(interfaceType)) {
                        registered = true

                        registerFocuser(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isRotator(interfaceType)) {
                        registered = true

                        registerRotator(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGPS(interfaceType)) {
                        registered = true

                        registerGPS(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isGuider(interfaceType)) {
                        registered = true

                        registerGuideOutput(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isLightBox(interfaceType)) {
                        registered = true

                        registerLightBox(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (DeviceInterfaceType.isDustCap(interfaceType)) {
                        registered = true

                        registerDustCap(driverInfo)?.also {
                            it.handleMessage(message)
                            takeMessageFromReorderingQueue(it)
                        }
                    }

                    if (!registered) {
                        LOG.w("device is not registered. name={}, interface={}", message.device, interfaceType)
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

                LOG.d("message received: {}", message)

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

                    LOG.d("message received: {}", message)

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

            LOG.d("message received: {}", message)
        } else {
            if (message in messageQueueCounter) {
                val counter = messageQueueCounter[message]!!

                if (counter < 2048) {
                    messageQueueCounter[message] = counter + 1
                    messageReorderingQueue.offer(message)
                } else {
                    messageReorderingQueue.remove(message)
                    LOG.w("message looping detected: {}", message)
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
