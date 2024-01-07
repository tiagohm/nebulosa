package nebulosa.api.atlas

import nebulosa.api.preferences.PreferenceService
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Component
class SatelliteUpdateTask(
    private val httpClient: OkHttpClient,
    private val preferenceService: PreferenceService,
    private val satelliteRepository: SatelliteRepository,
) : Runnable {

    @Scheduled(fixedDelay = UPDATE_INTERVAL, timeUnit = TimeUnit.MILLISECONDS)
    override fun run() {
        checkIsOutOfDateAndUpdate()
    }

    private fun isOutOfDate(): Boolean {
        val updatedAt = preferenceService.getLong(UPDATED_AT_KEY) ?: 0L
        return System.currentTimeMillis() - updatedAt >= UPDATE_INTERVAL
    }

    private fun checkIsOutOfDateAndUpdate() {
        if (isOutOfDate()) {
            LOG.info("satellites is out of date")

            if (updateTLEs()) {
                preferenceService.putLong(UPDATED_AT_KEY, System.currentTimeMillis())
            } else {
                LOG.warn("no satellites was updated")
            }
        } else {
            LOG.info("satellites is up to date")
        }
    }

    private fun updateTLEs(): Boolean {
        satelliteRepository.deleteAll()

        val data = HashMap<Long, SatelliteEntity>(16384)
        val tasks = ArrayList<CompletableFuture<*>>(SatelliteGroupType.entries.size)

        for (source in SatelliteGroupType.entries) {
            CompletableFuture
                .runAsync(TLEUpdater(source, data))
                .also(tasks::add)
        }

        tasks.forEach(CompletableFuture<*>::get)

        return satelliteRepository
            .save(data.values)
            .also { LOG.info("{} satellites updated", it.size) }
            .isNotEmpty()
    }

    private inner class TLEUpdater(
        private val group: SatelliteGroupType,
        private val data: MutableMap<Long, SatelliteEntity>,
    ) : Runnable {

        override fun run() {
            val request = Request.Builder()
                .get()
                .url("https://celestrak.org/NORAD/elements/gp.php?GROUP=${group.group}&FORMAT=tle")
                .build()

            httpClient.newCall(request)
                .execute().use {
                    if (it.isSuccessful) {
                        val lines = ArrayList<String>(3)

                        for (line in it.body!!.byteStream().bufferedReader().lines()) {
                            lines.add(line)

                            if (lines.size == 3) {
                                val id = lines[1].substring(2..6).toLong()

                                synchronized(data) {
                                    if (id in data) {
                                        data[id]!!.groups.add(group.name)
                                    } else {
                                        val name = lines[0].trim()
                                        val tle = lines.joinToString("\n")
                                        data[id] = SatelliteEntity(id, name, tle, mutableListOf(group.name))
                                    }
                                }

                                lines.clear()
                            }
                        }
                    }
                }
        }
    }

    companion object {

        const val UPDATE_INTERVAL = 1000L * 60 * 60 * 24 * 2 // 2 days in ms
        const val UPDATED_AT_KEY = "SATELLITES.UPDATED_AT"

        @JvmStatic private val LOG = loggerFor<SatelliteUpdateTask>()
    }
}
