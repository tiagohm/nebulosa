package nebulosa.alpaca.indi.device

import nebulosa.alpaca.api.AlpacaDeviceService
import nebulosa.alpaca.api.AlpacaResponse
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.common.time.Stopwatch
import nebulosa.indi.device.*
import nebulosa.log.loggerFor
import retrofit2.Call
import retrofit2.HttpException
import java.time.LocalDateTime
import java.util.*

abstract class ASCOMDevice : Device {

    protected abstract val device: ConfiguredDevice
    protected abstract val service: AlpacaDeviceService
    abstract override val sender: AlpacaClient

    override val name
        get() = device.name

    override val id
        get() = device.uid

    @Volatile final override var connected = false
        private set

    override val properties = emptyMap<String, PropertyVector<*, *>>()
    override val messages = LinkedList<String>()

    @Volatile private var refresher: Refresher? = null

    internal open fun initialize() {
        refresh(0L)

        if (refresher == null) {
            refresher = Refresher()
            refresher!!.start()
        }
    }

    override fun connect() {
        service.connect(device.number, true).doRequest()
    }

    override fun disconnect() {
        service.connect(device.number, false).doRequest()
    }

    open fun refresh(elapsedTimeInSeconds: Long) {
        service.isConnected(device.number).doRequest { processConnected(it.value) }
    }

    open fun reset() {
        connected = false
    }

    override fun close() {
        refresher?.interrupt()
        refresher = null
    }

    protected abstract fun onConnected()

    protected abstract fun onDisconnected()

    private fun addMessageAndFireEvent(text: String) {
        synchronized(messages) {
            messages.addFirst(text)

            sender.fireOnEventReceived(DeviceMessageReceived(this, text))

            if (messages.size > 100) {
                messages.removeLast()
            }
        }
    }

    protected fun <T : AlpacaResponse<*>> Call<T>.doRequest(): T? {
        try {
            val response = execute().body()

            return if (response == null) {
                LOG.warn("response has no body. device={}", name)
                null
            } else if (response.errorNumber != 0) {
                val message = response.errorMessage

                if (message.isNotEmpty()) {
                    addMessageAndFireEvent("[%s]: %s".format(LocalDateTime.now(), message))
                }

                // LOG.warn("unsuccessful response. device={}, code={}, message={}", name, response.errorNumber, response.errorMessage)

                null
            } else {
                response
            }
        } catch (e: HttpException) {
            LOG.error("unexpected response. device=$name", e)
        } catch (e: Throwable) {
            sender.fireOnConnectionClosed()
            LOG.error("unexpected error. device=$name", e)
        }

        return null
    }

    protected inline fun <T : AlpacaResponse<*>> Call<T>.doRequest(action: (T) -> Unit): Boolean {
        return doRequest()?.also(action) != null
    }

    protected fun processConnected(value: Boolean) {
        if (connected != value) {
            connected = value

            if (value) {
                sender.fireOnEventReceived(DeviceConnected(this))
                onConnected()
            } else {
                sender.fireOnEventReceived(DeviceDisconnected(this))
                onDisconnected()
            }
        }
    }

    private inner class Refresher : Thread("$name ASCOM Refresher") {

        private val stopwatch = Stopwatch()

        init {
            isDaemon = true
        }

        override fun run() {
            stopwatch.start()

            while (true) {
                val startTime = System.currentTimeMillis()
                if (connected) refresh(stopwatch.elapsedSeconds)
                val endTime = System.currentTimeMillis()
                val delayTime = 2000L - (endTime - startTime)

                if (delayTime > 1L) {
                    sleep(delayTime)
                }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ASCOMDevice>()
    }
}
