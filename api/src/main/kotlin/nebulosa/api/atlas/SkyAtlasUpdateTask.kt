package nebulosa.api.atlas

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.beans.annotations.ThreadedTask
import nebulosa.api.preferences.PreferenceService
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Component
@ThreadedTask
class SkyAtlasUpdateTask(
    private val objectMapper: ObjectMapper,
    private val preferenceService: PreferenceService,
    private val starsRepository: StarRepository,
    private val deepSkyObjectRepository: DeepSkyObjectRepository,
    private val httpClient: OkHttpClient,
    private val dataPath: Path,
) : Runnable {

    override fun run() {
        val version = preferenceService.skyAtlasVersion

        if (version != DATABASE_VERSION) {
            LOG.info("Star/DSO database is out of date. currentVersion={}, newVersion={}", version, DATABASE_VERSION)

            starsRepository.deleteAllInBatch()
            deepSkyObjectRepository.deleteAllInBatch()

            readStarsAndLoad()
            readDSOsAndLoad()

            preferenceService.skyAtlasVersion = DATABASE_VERSION
        } else {
            LOG.info("Star/DSO database is up to date")
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

        @JvmStatic private val LOG = loggerFor<SkyAtlasUpdateTask>()
    }
}
