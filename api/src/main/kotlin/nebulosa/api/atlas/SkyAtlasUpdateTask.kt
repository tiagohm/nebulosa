package nebulosa.api.atlas

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.source
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SkyAtlasUpdateTask(
    private val httpClient: OkHttpClient,
    private val simbadIdentifierRepository: SimbadIdentifierRepository,
) : Runnable {

    @Scheduled(fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        if (simbadIdentifierRepository.isEmpty()) {
            val request = Request.Builder()
                .url(URL)
                .build()

            httpClient.newCall(request)
                .execute()
                .use {
                    if (it.isSuccessful) {
                        val reader = SimbadIdentifierReader(it.body!!.byteStream().source())
                        val identifiers = ArrayList<SimbadIdentifierEntity>(10000)

                        synchronized(simbadIdentifierRepository) {
                            while (reader.hasNext()) {
                                identifiers.add(reader.next())

                                if (identifiers.size == 10000) {
                                    simbadIdentifierRepository.save(identifiers)
                                    identifiers.clear()
                                }
                            }
                        }
                    }
                }
        }
    }

    companion object {

        const val URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/simbad.ids"
        const val SIMBAD_IDENTIFIER_UPDATED_AT = "SIMBAD_IDENTIFIER_UPDATED_AT"
    }
}
