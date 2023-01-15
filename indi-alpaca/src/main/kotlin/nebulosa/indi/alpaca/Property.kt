package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device
import okhttp3.ConnectionPool
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal sealed class Property<T>(
    @JvmField protected val connection: AlpacaINDIConnection,
    @JvmField protected val device: Device,
) : AtomicReference<T>(), Callable<T>, Runnable {

    @JvmField internal var period = 5L

    final override fun run() {
        try {
            set(call())
        } catch (e: Throwable) {
            LOG.error("run failed", e)
        }
    }

    protected abstract fun parseBody(json: JSONObject): T

    protected fun requestGet(path: String): T {
        val clientId = device.hashCode()
        val clientTransactionId = TRANSACTION_ID_COUNTER.getAndIncrement()
        val uri = "${connection.host}:${connection.port}/api/v1/$path?ClientID=$clientId&ClientTransactionID=$clientTransactionId"

        val request = Request.Builder()
            .get().url(uri)
            .build()

        return CLIENT
            .newCall(request)
            .execute()
            .body
            .use { parseBody(JSONObject(it.string())) }
            .also {
                if (LOG.isDebugEnabled) {
                    LOG.debug("the {} GET request returned {}", uri, it)
                }
            }
    }

    protected fun requestPut(
        path: String,
        vararg data: Pair<String, Any>,
    ) {
        val clientId = device.hashCode()
        val clientTransactionId = TRANSACTION_ID_COUNTER.getAndIncrement()
        val uri = "${connection.host}:${connection.port}/api/v1/$path"

        val body = FormBody.Builder()
        body.add("ClientID", "$clientId")
        body.add("ClientTransactionID", "$clientTransactionId")
        data.forEach { body.add(it.first, "${it.second}") }

        val request = Request.Builder()
            .put(body.build()).url(uri)
            .build()

        return CLIENT
            .newCall(request)
            .execute()
            .body
            .use { }
    }

    companion object {

        @JvmStatic private val CONNECTION_POOL = ConnectionPool(32, 5L, TimeUnit.MINUTES)
        @JvmStatic private val CLIENT = OkHttpClient.Builder().connectionPool(CONNECTION_POOL).build()
        @JvmStatic private val TRANSACTION_ID_COUNTER = AtomicInteger(1)
        @JvmStatic private val LOG = LoggerFactory.getLogger(Property::class.java)
    }
}
