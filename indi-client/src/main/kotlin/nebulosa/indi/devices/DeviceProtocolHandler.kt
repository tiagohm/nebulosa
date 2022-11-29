package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.events.*
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.parser.INDIProtocolParser
import nebulosa.indi.protocol.parser.INDIProtocolReader
import java.util.concurrent.LinkedBlockingQueue

class DeviceProtocolHandler : INDIProtocolParser {

    @Volatile private var closed = false
    private val cameras = HashMap<String, Camera>(2)
    private val mounts = HashMap<String, Mount>(2)
    private val messageReorderingQueue = LinkedBlockingQueue<INDIProtocol>()
    private val notRegisteredDevices = HashSet<String>()
    private val protocolReader by lazy { INDIProtocolReader(this, Thread.MIN_PRIORITY) }
    private val messageQueueCounter = HashMap<INDIProtocol, Int>(2048)
    private val handlers = ArrayList<DeviceEventHandler>()

    override val input = object : INDIInputStream {

        override fun readINDIProtocol() = messageReorderingQueue.take()

        override fun close() = Unit
    }

    fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    internal fun fireOnEventReceived(device: Device, event: DeviceEvent<*>) {
        handlers.forEach { it.onEventReceived(device, event) }
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
            for (camera in cameras) {
                fireOnEventReceived(camera.value, CameraDetachedEvent(camera.value))
            }

            for (mount in mounts) {
                fireOnEventReceived(mount.value, MountDetachedEvent(mount.value))
            }

            cameras.clear()
            mounts.clear()

            notRegisteredDevices.clear()
            messageQueueCounter.clear()
            messageReorderingQueue.clear()
            handlers.clear()
        }
    }

    @Synchronized
    internal fun handleMessage(client: INDIClient, message: INDIProtocol) {
        if (closed) return

        if (message is DefTextVector) {
            if (message.name == "DRIVER_INFO") {
                val executable = message.elements.first { it.name == "DRIVER_EXEC" }.value

                var registered = false

                if (executable in Camera.DRIVERS) {
                    if (message.device !in cameras) {
                        val camera = Camera(client, this, message.device)
                        cameras[message.device] = camera
                        fireOnEventReceived(camera, CameraAttachedEvent(camera))
                        registered = true
                    } else {
                        registered = true
                    }
                }

                if (executable in Mount.DRIVERS) {
                    if (message.device !in mounts) {
                        val mount = Mount(client, this, message.device)
                        mounts[message.device] = mount
                        fireOnEventReceived(mount, MountAttachedEvent(mount))
                        registered = true
                    } else {
                        registered = true
                    }
                }

                if (!registered) {
                    notRegisteredDevices.add(message.device)
                }

                return
            }

            // TODO: Handle Guider. See which properties are used.
        }

        handleMessage(message)
    }

    @Synchronized
    override fun handleMessage(message: INDIProtocol) {
        if (closed) return

        if (message is Message) {
            return
        } else if (message is DelProperty) {
            // TODO: Handle delProperty (delete device or reset device property values)
            // TODO: call deviceRemoved(device)
            return
        }

        if (message.device.isEmpty() || message.device in notRegisteredDevices) {
            messageReorderingQueue.remove(message)
            return
        }

        var found = false

        if (message.device in cameras) {
            found = true
            cameras[message.device]!!.handleMessage(message)
        }

        if (message.device in mounts) {
            found = true
            mounts[message.device]!!.handleMessage(message)
        }

        if (!found) {
            if (message in messageQueueCounter) {
                val counter = messageQueueCounter[message]!!

                if (counter < 2048) {
                    messageQueueCounter[message] = counter + 1
                    messageReorderingQueue.offer(message)
                } else {
                    messageReorderingQueue.remove(message)
                    println("message looping detected: $message")
                }
            } else {
                messageQueueCounter[message] = 1
                messageReorderingQueue.offer(message)
            }
        } else {
            messageReorderingQueue.remove(message)
        }
    }
}
