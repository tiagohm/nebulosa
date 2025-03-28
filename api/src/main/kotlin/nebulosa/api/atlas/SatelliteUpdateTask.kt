package nebulosa.api.atlas

import com.fasterxml.jackson.databind.JsonMappingException
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.api.message.MessageService
import nebulosa.api.preference.PreferenceService
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class SatelliteUpdateTask(
    private val httpClient: OkHttpClient,
    private val satelliteRepository: SatelliteRepository,
    private val preferenceService: PreferenceService,
    private val messageService: MessageService,
    scheduledExecutorService: ScheduledExecutorService,
) : Runnable, KoinComponent {

    init {
        scheduledExecutorService.schedule(this, 10L, TimeUnit.SECONDS)
    }

    override fun run() {
        get<MainDatabaseMigrator>().await()
        get<SkyDatabaseMigrator>().await()

        checkIsOutOfDateAndUpdate()
    }

    private fun isOutOfDate(): Boolean {
        try {
            val updatedAt = preferenceService[UPDATED_AT_KEY]?.toLongOrNull() ?: 0L
            return System.currentTimeMillis() - updatedAt >= UPDATE_INTERVAL
        } catch (_: JsonMappingException) {
            return true
        }
    }

    private fun checkIsOutOfDateAndUpdate() {
        if (isOutOfDate()) {
            LOG.info("satellites is out of date")

            if (updateTLEs()) {
                preferenceService[UPDATED_AT_KEY] = System.currentTimeMillis()
                preferenceService.save()
            } else {
                LOG.warn("no satellites was updated")
            }
        } else {
            LOG.info("satellites is up to date")
        }
    }

    private fun updateTLEs(): Boolean {
        satelliteRepository.clear()

        messageService.sendMessage(SatelliteUpdateNotificationEvent.Started)

        val data = HashMap<Long, SatelliteEntity>(16384)
        val tasks = ArrayList<CompletableFuture<*>>(SatelliteGroupType.entries.size)

        for (source in SatelliteGroupType.entries) {
            CompletableFuture
                .runAsync(TLEUpdater(source, data))
                .also(tasks::add)
        }

        tasks.forEach(CompletableFuture<*>::get)

        return satelliteRepository
            .add(data.values)
            .also { LOG.info("{} satellites updated", it.size) }
            .also { messageService.sendMessage(SatelliteUpdateNotificationEvent.Finished(it.size)) }
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
                                        (data[id]!!.groups as MutableList<SatelliteGroupType>).add(group)
                                    } else {
                                        val name = lines[0].trim()
                                        val tle = lines.joinToString("\n")
                                        data[id] = SatelliteEntity(id, name, tle, mutableListOf(group))
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
        const val UPDATED_AT_KEY = "satellites.updatedAt"

        private val LOG = loggerFor<SatelliteUpdateTask>()
    }
}
