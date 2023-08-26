package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.TLEEntity
import nebulosa.api.data.entities.TLEEntity_
import nebulosa.api.data.entities.TLESourceEntity
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.function.Supplier

@Service
class TLERepository(
    boxStore: BoxStore,
    private val tleSourceRepository: TLESourceRepository,
    private val systemExecutorService: ExecutorService,
    private val okHttpClient: OkHttpClient,
) : BoxRepository<TLEEntity>() {

    override val box = boxStore.boxFor(TLEEntity::class.java)!!

    fun sources() = tleSourceRepository.all()

    fun withSources(source: TLESourceEntity): List<TLEEntity> = box.query()
        .equal(TLEEntity_.source, source.id)
        .build()
        .find()

    fun withName(name: String): List<TLEEntity> = box.query()
        .let { if (name.isBlank()) it else it.contains(TLEEntity_.name, name, CASE_INSENSITIVE) }
        .order(TLEEntity_.name)
        .build()
        .find()

    fun deleteWithSource(source: TLESourceEntity) = box.query()
        .equal(TLEEntity_.source, source.id)
        .build()
        .remove()

    @PostConstruct
    fun updateIfOld() {
        val currentTime = System.currentTimeMillis()

        for (source in sources()) {
            if (source.enabled && currentTime - source.updatedAt >= TLE_UPDATE_INTERVAL) {
                deleteWithSource(source)

                CompletableFuture
                    .supplyAsync(TLEUpdater(source), systemExecutorService)
                    .whenComplete { entities, e ->
                        e?.printStackTrace()

                        if (!entities.isNullOrEmpty()) {
                            saveAll(entities)

                            source.updatedAt = System.currentTimeMillis()
                            tleSourceRepository.save(source)

                            LOG.info("updated {} satellites from {}", entities.size, source.url)
                        }
                    }
            } else if (!source.enabled) {
                deleteWithSource(source)
            }
        }
    }

    private inner class TLEUpdater(private val source: TLESourceEntity) : Supplier<List<TLEEntity>> {

        override fun get(): List<TLEEntity> {
            val request = Request.Builder()
                .get()
                .url(source.url)
                .build()

            return okHttpClient.newCall(request)
                .execute().use {
                    if (it.isSuccessful) {
                        source.parseTLEFile(it.body.byteStream())
                    } else {
                        emptyList()
                    }
                }
        }
    }

    companion object {

        const val TLE_UPDATE_INTERVAL = 1000L * 60 * 60 * 72 // 72 hours in ms

        @JvmStatic private val LOG = loggerFor<TLERepository>()

        @JvmStatic
        internal fun TLESourceEntity.parseTLEFile(data: ByteArray): List<TLEEntity> {
            return parseTLEFile(ByteArrayInputStream(data))
        }

        @JvmStatic
        internal fun TLESourceEntity.parseTLEFile(data: InputStream): List<TLEEntity> {
            val entities = ArrayList<TLEEntity>(128)
            val lines = ArrayList<String>(3)

            for (line in data.bufferedReader().lines()) {
                lines.add(line)

                if (lines.size == 3) {
                    val tle = TLEEntity.from(this, lines)
                    lines.clear()
                    entities.add(tle)
                }
            }

            return entities
        }
    }
}
