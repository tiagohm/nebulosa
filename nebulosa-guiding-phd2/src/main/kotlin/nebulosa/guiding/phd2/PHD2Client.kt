package nebulosa.guiding.phd2

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.guiding.phd2.event.*
import okio.buffer
import okio.source
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

class PHD2Client(
    val host: String,
    val port: Int = 4400,
) : Closeable {

    private val socket = Socket()
    private val listeners = arrayListOf<EventListener>()

    @Volatile private var input: InputStream? = null
    @Volatile private var thread: Thread? = null

    fun registerListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: EventListener) {
        listeners.remove(listener)
    }

    fun start() {
        socket.connect(InetSocketAddress(host, port))
        input = socket.getInputStream()

        thread = Thread(::readEvent)
        thread!!.start()
    }

    override fun close() {
        thread?.interrupt()
        thread = null

        input = null

        socket.close()
    }

    private fun readEvent() {
        val buffer = input!!.source().buffer()

        try {
            while (true) {
                val eventText = buffer.readUtf8Line() ?: break
                val eventName = EVENT_NAME_REGEX.matchEntire(eventText)?.groupValues?.get(1) ?: continue

                if (LOG.isDebugEnabled) {
                    LOG.info("received: {}", eventText)
                }

                val type = EVENT_TYPES.keys.firstOrNull { it.simpleName == eventName } ?: continue
                val event = EVENT_TYPES[type] ?: OBJECT_MAPPER.readValue(eventText, type.java)
                listeners.forEach { it.onEvent(this, event) }
            }
        } catch (_: InterruptedException) {
        } catch (e: Throwable) {
            LOG.error("socket error", e)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(PHD2Client::class.java)

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        @JvmStatic private val EVENT_NAME_REGEX = Regex(".*\"Event\":\"(.*?)\".*")

        @JvmStatic private val EVENT_TYPES = mapOf(
            Alert::class to null,
            AppState::class to null,
            Calibrating::class to null,
            CalibrationComplete::class to null,
            CalibrationDataFlipped::class to null,
            CalibrationFailed::class to null,
            ConfigurationChange::class to ConfigurationChange,
            GuideParamChange::class to null,
            GuideStep::class to null,
            GuidingDithered::class to null,
            GuidingStopped::class to GuidingStopped,
            LockPositionLost::class to LockPositionLost,
            LockPositionSet::class to null,
            LockPositionShiftLimitReached::class to LockPositionShiftLimitReached,
            LoopingExposures::class to null,
            LoopingExposuresStopped::class to LoopingExposuresStopped,
            Paused::class to Paused,
            Resumed::class to Resumed,
            SettleBegin::class to SettleBegin,
            SettleDone::class to null,
            Settling::class to null,
            StarLost::class to null,
            StarSelected::class to null,
            StartCalibration::class to null,
            StartGuiding::class to StartGuiding,
            Version::class to null,
        )
    }
}
