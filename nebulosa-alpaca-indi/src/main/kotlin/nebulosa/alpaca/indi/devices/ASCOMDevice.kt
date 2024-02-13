package nebulosa.alpaca.indi.devices

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
    protected abstract val client: AlpacaClient

    @Suppress("PropertyName")
    @JvmField protected val LOG = loggerFor(javaClass)

    override val name
        get() = device.name

    val uid
        get() = device.uid

    @Volatile final override var connected = false
        private set

    override val properties = emptyMap<String, PropertyVector<*, *>>()
    override val messages = LinkedList<String>()

    @Volatile private var refresher: Refresher? = null

    override fun connect() {
        executeRequest(service.connect(device.number, true))
    }

    override fun disconnect() {
        executeRequest(service.connect(device.number, false))
    }

    open fun refresh(elapsedTimeInSeconds: Long) {
        executeRequest(service.isConnected(device.number)) { processConnected(it.value) }
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
        messages.addFirst(text)
        client.fireOnEventReceived(DeviceMessageReceived(this, text))
    }

    protected fun <T : AlpacaResponse<*>> executeRequest(call: Call<T>): T? {
        try {
            val response = call.execute().body()

            if (response == null) {
                LOG.warn("response has no body. device={}", name)
                return null
            } else if (response.errorNumber != 0) {
                val message = response.errorMessage

                if (message.isNotEmpty()) {
                    addMessageAndFireEvent("[%s]: %s".format(LocalDateTime.now(), message))
                }

                LOG.warn("unsuccessful response. device={}, code={}, message={}", name, response.errorNumber, response.errorMessage)
                return null
            } else {
                return response
            }
        } catch (e: HttpException) {
            LOG.error("unexpected response. device=$name", e)
        } catch (e: Throwable) {
            LOG.error("unexpected error. device=$name", e)
        }

        return null
    }

    protected inline fun <T : AlpacaResponse<*>> executeRequest(call: Call<T>, action: (T) -> Unit): Boolean {
        return executeRequest(call)?.also(action) != null
    }

    protected fun processConnected(value: Boolean) {
        if (connected != value) {
            connected = value

            if (value) {
                client.fireOnEventReceived(DeviceConnected(this))

                onConnected()

                if (refresher == null) {
                    refresher = Refresher()
                    refresher!!.start()
                }
            } else {
                client.fireOnEventReceived(DeviceDisconnected(this))

                onDisconnected()

                refresher?.interrupt()
                refresher = null
            }
        }
    }

    private inner class Refresher : Thread() {

        private val stopwatch = Stopwatch()

        override fun run() {
            stopwatch.start()

            val startTime = System.currentTimeMillis()
            refresh(stopwatch.elapsedSeconds)
            val endTime = System.currentTimeMillis()
            val delayTime = 1000L - (endTime - startTime)

            if (delayTime > 1L) {
                sleep(delayTime)
            }
        }
    }
}
