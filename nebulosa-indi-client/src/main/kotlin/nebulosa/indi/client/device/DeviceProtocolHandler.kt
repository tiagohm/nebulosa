package nebulosa.indi.client.device

import nebulosa.indi.client.device.AbstractDevice.Companion.create
import nebulosa.indi.client.device.camera.AsiCamera
import nebulosa.indi.client.device.camera.CameraDevice
import nebulosa.indi.client.device.camera.SimCamera
import nebulosa.indi.client.device.mount.IoptronV3Mount
import nebulosa.indi.client.device.mount.MountDevice
import nebulosa.indi.device.*
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSAttached
import nebulosa.indi.device.gps.GPSDetached
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.indi.protocol.DefTextVector
import nebulosa.indi.protocol.DelProperty
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Message
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.util.concurrent.LinkedBlockingQueue

abstract class DeviceProtocolHandler : MessageSender, INDIProtocolParser {

    @JvmField protected val cameras = HashMap<String, Camera>(2)
    @JvmField protected val mounts = HashMap<String, Mount>(1)
    @JvmField protected val wheels = HashMap<String, FilterWheel>(1)
    @JvmField protected val focusers = HashMap<String, Focuser>(2)
    @JvmField protected val gps = HashMap<String, GPS>(2)
    @JvmField protected val guideOutputs = HashMap<String, GuideOutput>(2)
    @JvmField protected val thermometers = HashMap<String, Thermometer>(2)
    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    @Volatile private var protocolReader: INDIProtocolReader? = null
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)
    private val handlers = ArrayList<DeviceEventHandler>()

    override val input = object : INDIInputStream {

        override fun readINDIProtocol(): INDIProtocol {
            Thread.sleep(1)
            return messageReorderingQueue.take()
        }

        override fun close() = Unit
    }

    val isRunning
        get() = protocolReader != null

    fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    fun fireOnEventReceived(event: DeviceEvent<*>) {
        handlers.forEach { it.onEventReceived(event) }
    }

    internal fun registerGuideOutput(device: GuideOutput) {
        guideOutputs[device.name] = device
        fireOnEventReceived(GuideOutputAttached(device))
    }

    internal fun unregisterGuideOutput(device: GuideOutput) {
        if (device.name in guideOutputs) {
            guideOutputs.remove(device.name)
            fireOnEventReceived(GuideOutputDetached(device))
        }
    }

    internal fun registerThermometer(device: Thermometer) {
        thermometers[device.name] = device
        fireOnEventReceived(ThermometerAttached(device))
    }

    internal fun unregisterThermometer(device: Thermometer) {
        if (device.name in thermometers) {
            thermometers.remove(device.name)
            fireOnEventReceived(ThermometerDetached(device))
        }
    }

    open fun start() {
        if (protocolReader == null) {
            protocolReader = INDIProtocolReader(this, Thread.MIN_PRIORITY)
            protocolReader!!.start()
        }
    }

    override fun close() {
        if (protocolReader == null) return

        try {
            protocolReader!!.close()
        } finally {
            protocolReader = null

            for ((_, device) in cameras) {
                device.close()
                LOG.info("camera detached: {}", device.name)
                fireOnEventReceived(CameraDetached(device))
            }

            for ((_, device) in mounts) {
                device.close()
                LOG.info("mount detached: {}", device.name)
                fireOnEventReceived(MountDetached(device))
            }

            for ((_, device) in wheels) {
                device.close()
                LOG.info("filter wheel detached: {}", device.name)
                fireOnEventReceived(FilterWheelDetached(device))
            }

            for ((_, device) in focusers) {
                device.close()
                LOG.info("focuser detached: {}", device.name)
                fireOnEventReceived(FocuserDetached(device))
            }

            for ((_, device) in gps) {
                device.close()
                LOG.info("gps detached: {}", device.name)
                fireOnEventReceived(GPSDetached(device))
            }

            cameras.clear()
            mounts.clear()
            wheels.clear()
            focusers.clear()
            gps.clear()

            notRegisteredDevices.clear()
            messageQueueCounter.clear()
            messageReorderingQueue.clear()
            handlers.clear()
        }
    }

    fun findDeviceByName(name: String): Device? {
        return cameras[name] ?: mounts[name] ?: wheels[name]
        ?: focusers[name] ?: gps[name]
    }

    @Synchronized
    override fun handleMessage(message: INDIProtocol) {
        if (message.device in notRegisteredDevices) return

        if (message is DefTextVector) {
            when (message.name) {
                "DRIVER_INFO" -> {
                    val executable = message.elements.first { it.name == "DRIVER_EXEC" }.value

                    var registered = false

                    if (executable in Camera.DRIVERS) {
                        registered = true

                        if (message.device !in cameras) {
                            val device = CAMERAS[executable]?.create(this, message.device)
                                ?: CameraDevice(this, message.device)
                            cameras[message.device] = device
                            LOG.info("camera attached: {}", device.name)
                            fireOnEventReceived(CameraAttached(device))
                        }
                    }

                    if (executable in Mount.DRIVERS) {
                        registered = true

                        if (message.device !in mounts) {
                            val device = MOUNTS[executable]?.create(this, message.device)
                                ?: MountDevice(this, message.device)
                            mounts[message.device] = device
                            LOG.info("mount attached: {}", device.name)
                            fireOnEventReceived(MountAttached(device))
                        }
                    }

                    if (executable in FilterWheel.DRIVERS) {
                        registered = true

                        if (message.device !in wheels) {
                            val device = FilterWheelDevice(this, message.device)
                            wheels[message.device] = device
                            LOG.info("filter wheel attached: {}", device.name)
                            fireOnEventReceived(FilterWheelAttached(device))
                        }
                    }

                    if (executable in Focuser.DRIVERS) {
                        registered = true

                        if (message.device !in focusers) {
                            val device = FocuserDevice(this, message.device)
                            focusers[message.device] = device
                            LOG.info("focuser attached: {}", device.name)
                            fireOnEventReceived(FocuserAttached(device))
                        }
                    }

                    if (executable in GPS.DRIVERS) {
                        registered = true

                        if (message.device !in gps) {
                            val device = GPSDevice(this, message.device)
                            gps[message.device] = device
                            LOG.info("gps attached: {}", device.name)
                            fireOnEventReceived(GPSAttached(device))
                        }
                    }

                    if (!registered) {
                        LOG.warn("device is not registered: {}", message.device)
                        notRegisteredDevices.add(message.device)
                    }

                    return
                }
            }
        }

        when (message) {
            is Message -> {
                val device = findDeviceByName(message.device)

                if (device == null) {
                    val text = "[%s]: %s\n".format(message.timestamp, message.message)
                    fireOnEventReceived(DeviceMessageReceived(null, text))
                } else {
                    device.handleMessage(message)
                }

                LOG.debug { "message received: $message" }

                return
            }
            is DelProperty -> {
                if (message.name.isEmpty() && message.device.isNotEmpty()) {
                    val device = findDeviceByName(message.device)

                    device?.close()

                    when (device) {
                        is Camera -> {
                            fireOnEventReceived(CameraDetached(device))
                            LOG.info("camera detached: {}", device.name)
                            cameras.remove(device.name)
                        }
                        is Mount -> {
                            fireOnEventReceived(MountDetached(device))
                            LOG.info("mount detached: {}", device.name)
                            mounts.remove(device.name)
                        }
                        is FilterWheel -> {
                            fireOnEventReceived(FilterWheelDetached(device))
                            LOG.info("filter wheel detached: {}", device.name)
                            wheels.remove(device.name)
                        }
                        is Focuser -> {
                            fireOnEventReceived(FocuserDetached(device))
                            LOG.info("focuser detached: {}", device.name)
                            focusers.remove(device.name)
                        }
                        is GPS -> {
                            fireOnEventReceived(GPSDetached(device))
                            LOG.info("gps detached: {}", device.name)
                            focusers.remove(device.name)
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

        val device = findDeviceByName(message.device)

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

        @JvmStatic private val LOG = loggerFor<DeviceProtocolHandler>()

        @JvmStatic private val CAMERAS = mapOf(
            "indi_asi_ccd" to AsiCamera::class.java,
            "indi_asi_single_ccd" to AsiCamera::class.java,
            "indi_simulator_ccd" to SimCamera::class.java,
            "indi_simulator_guide" to SimCamera::class.java,
        )

        @JvmStatic private val MOUNTS = mapOf(
            "indi_ioptronv3_telescope" to IoptronV3Mount::class.java,
        )
    }
}
