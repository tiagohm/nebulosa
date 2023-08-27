package nebulosa.api.repositories

import jakarta.annotation.PostConstruct
import nebulosa.api.data.enums.SatelliteGroupType
import nebulosa.api.data.responses.SatelliteResponse
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

@Service
class SatelliteRepository(
    private val systemExecutorService: ExecutorService,
    private val okHttpClient: OkHttpClient,
) {

    private val data = HashMap<Long, SatelliteResponse>()

    fun search(text: String = "", groups: List<SatelliteGroupType>): List<SatelliteResponse> {
        return data.values
            .asSequence()
            .filter { text.isBlank() || it.name.contains(text, true) }
            .filter { !Collections.disjoint(groups, it.groups) }
            .toList()
    }

    @PostConstruct
    fun update() {
        data.clear()

        for (source in SatelliteGroupType.entries) {
            CompletableFuture
                .runAsync(TLEUpdater(source), systemExecutorService)
                .whenComplete { _, e ->
                    e?.printStackTrace()
                    LOG.info("updated {} satellites", data.size)
                }
        }
    }

    private inner class TLEUpdater(private val group: SatelliteGroupType) : Runnable {

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
                                        data[id]!!.groups.add(group)
                                    } else {
                                        val name = lines[0].trim()
                                        val tle = lines.joinToString("\n")
                                        data[id] = SatelliteResponse(id, name, tle, hashSetOf(group))
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

        @JvmStatic private val LOG = loggerFor<SatelliteRepository>()
    }
}
