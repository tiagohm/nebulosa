package nebulosa.api.atlas

import nebulosa.api.message.MessageService
import nebulosa.api.preference.PreferenceService
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
    private val messageService: MessageService,
) : Runnable {

    @Scheduled(fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        var request = Request.Builder().get().url(VERSION_URL).build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string().trim()

                if (newestVersion != preferenceService.getText(VERSION_KEY) || simbadEntityRepository.isEmpty()) {
                    LOG.info("Sky Atlas database is out of date. Downloading...")

                    messageService.sendMessage(SkyAtlasUpdateNotificationEvent.Started)

                    var finished = false

                    for (i in 0 until MAX_DATA_COUNT) {
                        if (finished) break

                        val url = DATA_URL.format(i)
                        request = Request.Builder().get().url(url).build()

                        httpClient.newCall(request).execute().use {
                            if (it.isSuccessful) {
                                it.body!!.byteStream().source().use { source ->
                                    SimbadDatabaseReader(source).use { reader ->
                                        for (entity in reader) {
                                            simbadEntityRepository.save(entity)
                                        }
                                    }
                                }
                            } else if (it.code == 404) {
                                finished = true
                            } else {
                                messageService.sendMessage(SkyAtlasUpdateNotificationEvent.Failed)

                                LOG.error("Failed to download. url={}, code={}", url, it.code)
                                return
                            }
                        }
                    }

                    preferenceService.putText(VERSION_KEY, newestVersion)
                    messageService.sendMessage(SkyAtlasUpdateNotificationEvent.Finished(newestVersion))

                    LOG.info("Sky Atlas database was updated. version={}, size={}", newestVersion, simbadEntityRepository.size)
                } else {
                    LOG.info("Sky Atlas database is up to date. version={}, size={}", newestVersion, simbadEntityRepository.size)
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
