package nebulosa.api.atlas

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.messages.MessageService
import nebulosa.api.notifications.NotificationEvent
import nebulosa.api.preferences.PreferenceService
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Component
class SkyAtlasUpdateTask(
    private val objectMapper: ObjectMapper,
    private val preferenceService: PreferenceService,
    private val starsRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val httpClient: OkHttpClient,
    private val dataPath: Path,
    private val satelliteUpdateTask: SatelliteUpdateTask,
    private val messageService: MessageService,
) : Runnable {

    data class Finished(override val body: String) : NotificationEvent {

        override val type = "SKY_ATLAS_UPDATE_FINISHED"
    }

    @Scheduled(initialDelay = 1L, fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        satelliteUpdateTask.run()

        val version = preferenceService.getText(SKY_ATLAS_VERSION)

        if (version != DATABASE_VERSION) {
            LOG.info("Star/DSO database is out of date. currentVersion={}, newVersion={}", version, DATABASE_VERSION)

            messageService.sendMessage(Finished("Star/DSO database is being updated."))

            starsRepository.deleteAllInBatch()
            deepSkyObjectRepository.deleteAllInBatch()

            readStarsAndLoad()
            readDSOsAndLoad()

            preferenceService.putText(SKY_ATLAS_VERSION, DATABASE_VERSION)

            messageService.sendMessage(Finished("Sky Atlas database was updated to version $DATABASE_VERSION."))
        } else {
            LOG.info("Star/DSO database is up to date. version={}", version)
        }
    }

    private fun readStarsAndLoad() {
        val starsPath = Path.of("$dataPath", "stars.json.gz")

        if (starsPath.exists()) {
            starsPath.inputStream().use(::loadStars)
        } else {
            val request = Request.Builder()
                .url("https://github.com/tiagohm/nebulosa/raw/main/api/data/stars.json.gz")
                .build()

            httpClient.newCall(request)
                .execute()
                .use {
                    if (it.isSuccessful) {
                        loadStars(it.body!!.byteStream())
                    }
                }
        }
    }

    private fun loadStars(inputStream: InputStream) {
        GZIPInputStream(inputStream)
            .use { objectMapper.readValue(it, object : TypeReference<List<StarEntity>>() {}) }
            .let(starsRepository::saveAllAndFlush)
            .also { LOG.info("Star database loaded. size={}", it.size) }
    }

    private fun readDSOsAndLoad() {
        val dsosPath = Path.of("$dataPath", "dsos.json.gz")

        if (dsosPath.exists()) {
            dsosPath.inputStream().use(::loadDSOs)
        } else {
            val request = Request.Builder()
                .url("https://github.com/tiagohm/nebulosa/raw/main/api/data/dsos.json.gz")
                .build()

            httpClient.newCall(request)
                .execute()
                .use {
                    if (it.isSuccessful) {
                        loadDSOs(it.body!!.byteStream())
                    }
                }
        }
    }

    private fun loadDSOs(inputStream: InputStream) {
        GZIPInputStream(inputStream)
            .use { objectMapper.readValue(it, object : TypeReference<List<DeepSkyObjectEntity>>() {}) }
            .let(deepSkyObjectRepository::saveAllAndFlush)
            .also { LOG.info("DSO database loaded. size={}", it.size) }
    }

    companion object {

        const val DATABASE_VERSION = "2023.10.18"
        const val SKY_ATLAS_VERSION = "SKY_ATLAS_VERSION"

        @JvmStatic private val LOG = loggerFor<SkyAtlasUpdateTask>()
    }
}
