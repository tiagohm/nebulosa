package nebulosa.indi.alpaca.device

import nebulosa.indi.alpaca.AlpacaINDIConnection
import nebulosa.indi.alpaca.Command
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal abstract class Device(
    @JvmField protected val connection: AlpacaINDIConnection,
    @JvmField val id: String,
    @JvmField val name: String,
    @JvmField val number: Int,
) {

    protected val connected by lazy { Connected(connection, this) }

    init {
        register(connected)
    }

    protected fun register(command: Command<*>) {
        synchronized(REGISTERED_PROPERTIES) {
            REGISTERED_PROPERTIES[command] = EXECUTOR.scheduleAtFixedRate(command, 1L, command.period, TimeUnit.SECONDS)
            LOG.info("{} property registered at fixed rate of {}s", command::class.simpleName, command.period)
        }
    }

    protected fun unregister(command: Command<*>) {
        synchronized(REGISTERED_PROPERTIES) {
            REGISTERED_PROPERTIES[command]?.cancel(true) ?: return
            REGISTERED_PROPERTIES.remove(command)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(Device::class.java)
        @JvmStatic private val EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
        @JvmStatic private val REGISTERED_PROPERTIES = HashMap<Command<*>, ScheduledFuture<*>>(512)
    }
}
