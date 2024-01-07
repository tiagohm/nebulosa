package nebulosa.api.atlas

import nebulosa.api.preferences.PreferenceService
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.source
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SkyAtlasUpdateTask(
    private val httpClient: OkHttpClient,
    private val simbadEntityRepository: SimbadEntityRepository,
    private val preferenceService: PreferenceService,
) : Runnable {

    @Scheduled(fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        var request = Request.Builder().get().url(VERSION_URL).build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string()

                if (newestVersion != preferenceService.getText(VERSION_KEY) || simbadEntityRepository.isEmpty()) {
                    LOG.info("Sky Atlas is out of date. Downloading...")

                    var finished = false

                    for (i in 0 until MAX_DATA_COUNT) {
                        if (finished) break

                        val url = DATA_URL.format(i)
                        request = Request.Builder().get().url(url).build()

                        httpClient.newCall(request).execute().use {
                            if (it.isSuccessful) {
                                val reader = SimbadDatabaseReader(it.body!!.byteStream().source())

                                for (entity in reader) {
                                    simbadEntityRepository.save(entity)
                                }
                            } else if (it.code == 404) {
                                finished = true
                            } else {
                                LOG.error("Failed to download. url={}, code={}", url, it.code)
                                return
                            }
                        }
                    }

                    preferenceService.putText(VERSION_KEY, newestVersion)
                } else {
                    LOG.info("Sky Atlas is up to date. version={}, size={}", newestVersion, simbadEntityRepository.size)
                }
            }
        }
    }

    companion object {

        const val VERSION_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/simbad/VERSION.txt"
        const val VERSION_KEY = "SKY_ATLAS.VERSION"

        const val DATA_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/simbad/simbad.%02d.dat"
        const val MAX_DATA_COUNT = 100

        @JvmStatic private val LOG = loggerFor<SkyAtlasUpdateTask>()
    }
}
