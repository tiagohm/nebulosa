package nebulosa.phd2.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.log.loggerFor
import nebulosa.phd2.client.event.*
import okio.buffer
import okio.source
import java.io.Closeable
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

class PHD2Client(
    val host: String,
    val port: Int = 4400,
) : Closeable {

    private val listeners = hashSetOf<PHD2EventListener>()

    @Volatile private var socket: Socket? = null
    @Volatile private var input: InputStream? = null
    @Volatile private var thread: Thread? = null

    fun registerListener(listener: PHD2EventListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: PHD2EventListener) {
        listeners.remove(listener)
    }

    fun connect() {
        require(socket == null) { "socket is already open" }

        val socket = Socket()
        socket.connect(InetSocketAddress(host, port))
        input = socket.getInputStream()
        this.socket = socket

        thread = Thread(::readEvent)
        thread!!.isDaemon = true
        thread!!.start()
    }

    override fun close() {
        thread?.interrupt()
        thread = null

        input = null

        socket?.close()
        socket = null

        listeners.clear()
    }

    private fun readEvent() {
        val buffer = input!!.source().buffer()

        try {
            while (true) {
                val eventText = buffer.readUtf8Line() ?: break
                val eventName = EVENT_NAME_REGEX.matchEntire(eventText)?.groupValues?.get(1) ?: continue

                if (LOG.isDebugEnabled) {
                    LOG.info("event received. event={}", eventText)
                }

                val type = EVENT_TYPES[eventName] ?: continue
                val event = type.second ?: OBJECT_MAPPER.readValue(eventText, type.first)
                listeners.forEach { it.onEvent(this, event) }
            }
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {
            LOG.error("event read error", e)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PHD2Client>()

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        @JvmStatic private val EVENT_NAME_REGEX = Regex(".*\"Event\":\"(.*?)\".*")

        @JvmStatic private val EVENT_TYPES = mapOf(
            "Alert" to (Alert::class.java to null),
            "AppState" to (AppState::class.java to null),
            "Calibrating" to (Calibrating::class.java to null),
            "CalibrationComplete" to (CalibrationComplete::class.java to null),
            "CalibrationDataFlipped" to (CalibrationDataFlipped::class.java to null),
            "CalibrationFailed" to (CalibrationFailed::class.java to null),
            "ConfigurationChange" to (ConfigurationChange::class.java to ConfigurationChange),
            "GuideParamChange" to (GuideParamChange::class.java to null),
            "GuideStep" to (GuideStep::class.java to null),
            "GuidingDithered" to (GuidingDithered::class.java to null),
            "GuidingStopped" to (GuidingStopped::class.java to GuidingStopped),
            "LockPositionLost" to (LockPositionLost::class.java to LockPositionLost),
            "LockPositionSet" to (LockPositionSet::class.java to null),
            "LockPositionShiftLimitReached" to (LockPositionShiftLimitReached::class.java to LockPositionShiftLimitReached),
            "LoopingExposures" to (LoopingExposures::class.java to null),
            "LoopingExposuresStopped" to (LoopingExposuresStopped::class.java to LoopingExposuresStopped),
            "Paused" to (Paused::class.java to Paused),
            "Resumed" to (Resumed::class.java to Resumed),
            "SettleBegin" to (SettleBegin::class.java to SettleBegin),
            "SettleDone" to (SettleDone::class.java to null),
            "Settling" to (Settling::class.java to null),
            "StarLost" to (StarLost::class.java to null),
            "StarSelected" to (StarSelected::class.java to null),
            "StartCalibration" to (StartCalibration::class.java to null),
            "StartGuiding" to (StartGuiding::class.java to StartGuiding),
            "Version" to (Version::class.java to null),
        )
    }
}
