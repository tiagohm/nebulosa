package nebulosa.alpaca.indi.device

import nebulosa.alpaca.api.AlpacaDeviceService
import nebulosa.alpaca.api.AlpacaResponse
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceConnected
import nebulosa.indi.device.DeviceDisconnected
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.PropertyVector
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.time.SystemClock
import nebulosa.util.Resettable
import nebulosa.util.time.Stopwatch
import retrofit2.Call
import retrofit2.HttpException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis

abstract class ASCOMDevice : Device, Resettable {

    protected abstract val device: ConfiguredDevice
    protected abstract val service: AlpacaDeviceService
    abstract override val sender: AlpacaClient

    override val name
        get() = device.name

    override val id
        get() = device.uid

    @Volatile final override var connected = false
        private set

    @Volatile final override var driver = ASCOMDriverInfo.EMPTY
        private set

    override val properties = emptyMap<String, PropertyVector<*, *>>()
    override val messages = LinkedList<String>()

    private val refresher = AtomicReference<Refresher>()

    internal open fun initialize() {
        processDriverInfo()

        refresh(0L)

        if (refresher.get() == null) {
            with(Refresher()) {
                refresher.set(this)
                start()
            }
        }
    }

    override fun connect() {
        service.connect(device.number, true).doRequest()
    }

    override fun disconnect() {
        service.connect(device.number, false).doRequest()
    }

    open fun refresh(elapsedTimeInSeconds: Long) {
        processConnected()
    }

    override fun reset() {
        connected = false
    }

    override fun close() {
        refresher.getAndSet(null)?.interrupt()
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
            val request = request()
            val response = execute()
            val body = response.body()

            return if (body == null) {
                LOG.d { debug("response has no body. device={}, request={} {}, response={}", name, request.method, request.url, response) }
                null
            } else if (body.errorNumber != 0) {
                val message = body.errorMessage

                if (message.isNotEmpty()) {
                    addMessageAndFireEvent("[%s]: %s".format(LocalDateTime.now(SystemClock), message))
                }

                LOG.d { debug("unsuccessful response. device={}, request={} {}, errorNumber={}, message={}", name, request.method, request.url, body.errorNumber, body.errorMessage) }

                null
            } else {
                body
            }
        } catch (e: HttpException) {
            LOG.error("unexpected response. device={}", name, e)
        } catch (e: Throwable) {
            sender.fireOnConnectionClosed()
            LOG.error("unexpected error. device={}", name, e)
        }

        return null
    }

    protected inline fun <T : AlpacaResponse<*>> Call<T>.doRequest(action: (T) -> Unit): Boolean {
        return doRequest()?.also(action) != null
    }

    private fun processConnected() {
        service.isConnected(device.number).doRequest { processConnected(it.value) }
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

    private fun processDriverInfo() {
        val name = service.driverInfo(device.number).doRequest()?.value?.trim()?.trimEnd('.') ?: ""
        val version = service.driverVersion(device.number).doRequest()?.value?.trim()?.trimStart('v', 'V') ?: ""
        driver = ASCOMDriverInfo(name, version)
    }

    private inner class Refresher : Thread("$name ASCOM Refresher") {

        private val stopwatch = Stopwatch()

        init {
            isDaemon = true
        }

        override fun run() {
            stopwatch.start()

            while (true) {
                val elapsedTime = measureTimeMillis {
                    refresh(stopwatch.elapsedSeconds)
                }

                val delayTime = 1500L - elapsedTime

                if (delayTime >= 10L) {
                    sleep(delayTime)
                }
            }
        }
    }

    companion object {

        private val LOG = loggerFor<ASCOMDevice>()
    }
}
