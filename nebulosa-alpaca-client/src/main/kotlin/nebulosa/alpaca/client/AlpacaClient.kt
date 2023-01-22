package nebulosa.alpaca.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.indi.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.protocol.INDIProtocol
import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue

class AlpacaClient(url: String) : MessageSender, Closeable {

    @Volatile private var closed = false

    private val service by lazy { AlpacaService(url) }
    private val handlers = arrayListOf<DeviceProtocolHandler>()
    private val messageQueue = LinkedBlockingQueue<INDIProtocol>()
    private val devices = arrayListOf<Device>()

    fun start() {
        check(!closed) { "closed" }

        service.management
            .configuredDevices()
            .execute()
            .body()
            ?.value
            ?.forEach { devices.add(Device(it, service, this)) }
    }

    private fun handleMessage(message: INDIProtocol) {
        handlers.forEach { it.handleMessage(this, message) }
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        TODO("Not yet implemented")
    }

    override fun close() {
        if (closed) return

        closed = true

        handlers.forEach(Closeable::close)
        handlers.clear()
    }

    private inner class AlpacaMonitor : Thread() {

        @Volatile private var running = false
        @Volatile private var count = 0

        override fun run() {
            running = true

            while (running) {
                for (device in devices) {
                    device.process(count)
                }

                count++

                sleep(1000L)
            }
        }
    }
}
