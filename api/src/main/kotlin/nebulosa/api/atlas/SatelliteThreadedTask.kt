package nebulosa.api.atlas

import nebulosa.api.beans.annotations.ThreadedTask
import nebulosa.api.configs.ConfigRepository
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
@ThreadedTask
class SatelliteThreadedTask(
    private val okHttpClient: OkHttpClient,
    private val configRepository: ConfigRepository,
    private val satelliteRepository: SatelliteRepository,
) : Runnable {

    override fun run() {
        checkIsOutOfDateAndUpdate()
    }

    private fun isOutOfDate(): Boolean {
        val updatedAt = configRepository.long(TLE_UPDATED_AT) ?: 0L
        return System.currentTimeMillis() - updatedAt >= UPDATE_INTERVAL
    }

    private fun checkIsOutOfDateAndUpdate() {
        if (isOutOfDate()) {
            LOG.info("satellites is out of date")

            if (updateTLEs()) {
                configRepository.save(TLE_UPDATED_AT, System.currentTimeMillis())
            } else {
                LOG.warn("no satellites was updated")
            }
        } else {
            LOG.info("satellites is up to date")
        }
    }

    private fun updateTLEs(): Boolean {
        val data = HashMap<Long, SatelliteEntity>(16384)
        val tasks = ArrayList<CompletableFuture<*>>(SatelliteGroupType.entries.size)

        for (source in SatelliteGroupType.entries) {
            CompletableFuture
                .runAsync(TLEUpdater(source, data))
                .also(tasks::add)
        }

        tasks.forEach(CompletableFuture<*>::get)

        return satelliteRepository
            .saveAllAndFlush(data.values)
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

            okHttpClient.newCall(request)
                .execute().use {
                    if (it.isSuccessful) {
                        val lines = ArrayList<String>(3)

                        for (line in it.body.byteStream().bufferedReader().lines()) {
                            lines.add(line)

                            if (lines.size == 3) {
                                val id = lines[1].substring(2..6).toLong()

                                synchronized(data) {
                                    if (id in data) {
                                        data[id]!!.groupType = data[id]!!.groupType or (1L shl group.ordinal)
                                    } else {
                                        val name = lines[0].trim()
                                        val tle = lines.joinToString("\n")
                                        data[id] = SatelliteEntity(id, name, tle, group.ordinal.toLong())
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

        const val TLE_UPDATED_AT = "TLE_UPDATED_AT"
        const val UPDATE_INTERVAL = 1000L * 60 * 60 * 24 // 1 day

        @JvmStatic private val LOG = loggerFor<SatelliteThreadedTask>()
    }
}
