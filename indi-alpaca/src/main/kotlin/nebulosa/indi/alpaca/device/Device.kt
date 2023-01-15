package nebulosa.indi.alpaca.device

import nebulosa.indi.alpaca.AlpacaINDIConnection
import nebulosa.indi.alpaca.Property
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal abstract class Device(
    @JvmField protected val connection: AlpacaINDIConnection,
    @JvmField val id: String,
    @JvmField val name: String,
) {

    protected val connected by lazy { Connected(connection, this) }

    init {
        register(connected)
    }

    protected fun register(property: Property<*>) {
        synchronized(REGISTERED_PROPERTIES) {
            REGISTERED_PROPERTIES[property] = EXECUTOR.scheduleAtFixedRate(property, 1L, property.period, TimeUnit.SECONDS)
            LOG.info("{} property registered at fixed rate of {}s", property::class.simpleName, property.period)
        }
    }

    protected fun unregister(property: Property<*>) {
        synchronized(REGISTERED_PROPERTIES) {
            REGISTERED_PROPERTIES[property]?.cancel(true) ?: return
            REGISTERED_PROPERTIES.remove(property)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(Device::class.java)
        @JvmStatic private val EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
        @JvmStatic private val REGISTERED_PROPERTIES = HashMap<Property<*>, ScheduledFuture<*>>(512)
    }
}
