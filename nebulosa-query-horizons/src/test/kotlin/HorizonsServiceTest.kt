import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.io.source
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.query.horizons.HorizonsService
import okio.ByteString.Companion.decodeBase64
import java.time.LocalDateTime

class HorizonsServiceTest : StringSpec() {

    init {
        val service = HorizonsService()

        "spk" {
            val start = LocalDateTime.of(2023, 1, 1, 0, 0)
            val end = LocalDateTime.of(2023, 12, 31, 23, 59)
            val spkFile = service.spk(1003517, start, end).execute().body().shouldNotBeNull()
            spkFile.id shouldBeExactly 1003517
            val spkBytes = spkFile.spk.decodeBase64()
            val spk = Spk(SourceDaf(spkBytes!!.asByteBuffer().source()))
            spk.shouldHaveSize(1)
            spk[10, 1003517].shouldNotBeNull()
        }
    }
}
