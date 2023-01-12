package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraBase
import nebulosa.indi.devices.cameras.CameraDetached
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.filterwheels.FilterWheelAttached
import nebulosa.indi.devices.filterwheels.FilterWheelBase
import nebulosa.indi.devices.filterwheels.FilterWheelDetached
import nebulosa.indi.devices.focusers.Focuser
import nebulosa.indi.devices.focusers.FocuserAttached
import nebulosa.indi.devices.focusers.FocuserBase
import nebulosa.indi.devices.focusers.FocuserDetached
import nebulosa.indi.devices.gps.GPS
import nebulosa.indi.devices.gps.GPSAttached
import nebulosa.indi.devices.gps.GPSBase
import nebulosa.indi.devices.gps.GPSDetached
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountAttached
import nebulosa.indi.devices.mounts.MountBase
import nebulosa.indi.devices.mounts.MountDetached
import nebulosa.indi.protocol.DefTextVector
import nebulosa.indi.protocol.DelProperty
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.Message
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

class DeviceProtocolHandler : INDIProtocolParser {

    @Volatile private var closed = false
    private val cameras = HashMap<String, Camera>(2)
    private val mounts = HashMap<String, Mount>(1)
    private val filterWheels = HashMap<String, FilterWheel>(1)
    private val focusers = HashMap<String, Focuser>(2)
    private val gps = HashMap<String, GPS>(2)
    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    private val protocolReader by lazy { INDIProtocolReader(this, Thread.MIN_PRIORITY) }
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)
    private val handlers = ArrayList<DeviceEventHandler>()

    override val input = object : INDIInputStream {

        override fun readINDIProtocol(): INDIProtocol {
            Thread.sleep(1)
            return messageReorderingQueue.take()
        }

        override fun close() = Unit
    }

    fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    fun fireOnEventReceived(event: DeviceEvent<*>) {
        handlers.forEach { it.onEventReceived(event) }
    }

    fun start() {
        protocolReader.start()
    }

    override fun close() {
        if (closed) return

        closed = true

        try {
            protocolReader.close()
        } finally {
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

            for ((_, device) in filterWheels) {
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
            filterWheels.clear()
            focusers.clear()
            gps.clear()

            notRegisteredDevices.clear()
            messageQueueCounter.clear()
            messageReorderingQueue.clear()
            handlers.clear()
        }
    }

    @Synchronized
    internal fun handleMessage(client: INDIClient, message: INDIProtocol) {
        if (closed) return

        if (message.device in notRegisteredDevices) return

        if (message is DefTextVector) {
            if (message.name == "DRIVER_INFO") {
                val executable = message.elements.first { it.name == "DRIVER_EXEC" }.value

                var registered = false

                if (executable in Camera.DRIVERS) {
                    registered = true

                    if (message.device !in cameras) {
                        val device = CameraBase(client, this, message.device)
                        cameras[message.device] = device
                        LOG.info("camera attached: {}", device.name)
                        fireOnEventReceived(CameraAttached(device))
                    }
                }

                if (executable in Mount.DRIVERS) {
                    registered = true

                    if (message.device !in mounts) {
                        val device = MountBase(client, this, message.device)
                        mounts[message.device] = device
                        LOG.info("mount attached: {}", device.name)
                        fireOnEventReceived(MountAttached(device))
                    }
                }

                if (executable in FilterWheel.DRIVERS) {
                    registered = true

                    if (message.device !in filterWheels) {
                        val device = FilterWheelBase(client, this, message.device)
                        filterWheels[message.device] = device
                        LOG.info("filter wheel attached: {}", device.name)
                        fireOnEventReceived(FilterWheelAttached(device))
                    }
                }

                if (executable in Focuser.DRIVERS) {
                    registered = true

                    if (message.device !in focusers) {
                        val device = FocuserBase(client, this, message.device)
                        focusers[message.device] = device
                        LOG.info("focuser attached: {}", device.name)
                        fireOnEventReceived(FocuserAttached(device))
                    }
                }

                if (executable in GPS.DRIVERS) {
                    registered = true

                    if (message.device !in gps) {
                        val device = GPSBase(client, this, message.device)
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

        handleMessage(message)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun findDeviceByName(name: String): Device? {
        return cameras[name] ?: mounts[name] ?: filterWheels[name]
        ?: focusers[name] ?: gps[name]
    }

    @Synchronized
    override fun handleMessage(message: INDIProtocol) {
        if (closed) return

        if (message is Message) {
            val device = findDeviceByName(message.device)

            if (device == null) {
                val text = "[%s]: %s\n".format(message.timestamp, message.message)
                handlers.forEach { it.onEventReceived(DeviceMessageReceived(null, text)) }
            } else {
                device.handleMessage(message)
            }

            if (LOG.isDebugEnabled) {
                LOG.debug("message received: {}", message)
            }

            return
        } else if (message is DelProperty) {
            if (message.name.isEmpty() && message.device.isNotEmpty()) {
                val device = findDeviceByName(message.device)

                device?.close()

                when (device) {
                    is Camera -> {
                        handlers.forEach { it.onEventReceived(CameraDetached(device)) }
                        LOG.info("camera detached: {}", device.name)
                        cameras.remove(device.name)
                    }
                    is Mount -> {
                        handlers.forEach { it.onEventReceived(MountDetached(device)) }
                        LOG.info("mount detached: {}", device.name)
                        mounts.remove(device.name)
                    }
                    is FilterWheel -> {
                        handlers.forEach { it.onEventReceived(FilterWheelDetached(device)) }
                        LOG.info("filter wheel detached: {}", device.name)
                        filterWheels.remove(device.name)
                    }
                    is Focuser -> {
                        handlers.forEach { it.onEventReceived(FocuserDetached(device)) }
                        LOG.info("focuser detached: {}", device.name)
                        focusers.remove(device.name)
                    }
                    is GPS -> {
                        handlers.forEach { it.onEventReceived(GPSDetached(device)) }
                        LOG.info("gps detached: {}", device.name)
                        focusers.remove(device.name)
                    }
                }

                return
            }
        }

        if (message.device.isEmpty() || message.device in notRegisteredDevices) {
            messageReorderingQueue.remove(message)
            return
        }

        val device = findDeviceByName(message.device)

        if (device != null) {
            device.handleMessage(message)

            messageReorderingQueue.remove(message)

            if (LOG.isDebugEnabled) {
                LOG.debug("message received: {}", message)
            }
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

        @JvmStatic private val LOG = LoggerFactory.getLogger(DeviceProtocolHandler::class.java)
    }
}
