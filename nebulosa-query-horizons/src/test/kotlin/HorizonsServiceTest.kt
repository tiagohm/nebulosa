import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.io.source
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance.Companion.m
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.query.horizons.HorizonsQuantity
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
        "observer" {
            val ephemeris = service
                .observer(
                    "499",
                    138.73119026648095.deg, 35.36276754848444.deg, 3776.m,
                    LocalDateTime.of(2022, 12, 25, 22, 0, 0),
                    LocalDateTime.of(2022, 12, 25, 23, 0, 0),
                    extraPrecision = true,
                ).execute()
                .body().shouldNotBeNull()

            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldBe "67.876674963"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldBe "24.657460871"
            ephemeris[dateTime]!![HorizonsQuantity.CONSTELLATION] shouldBe "Tau"
            ephemeris[dateTime]!![HorizonsQuantity.ILLUMINATED_FRACTION] shouldBe "98.32712"
            ephemeris[dateTime]!![HorizonsQuantity.VISUAL_MAGNITUDE] shouldBe "-1.426"
            ephemeris[dateTime]!![HorizonsQuantity.SURFACE_BRIGHTNESS] shouldBe "4.239"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_AZ] shouldBe "317.889826208"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_ALT] shouldBe "-16.319680741"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_HOUR_ANGLE] shouldBe "8.993056236"
        }
    }
}
