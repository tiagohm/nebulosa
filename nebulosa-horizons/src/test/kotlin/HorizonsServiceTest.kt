import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import nebulosa.horizons.HorizonsQuantity
import nebulosa.horizons.HorizonsService
import nebulosa.io.source
import nebulosa.math.deg
import nebulosa.math.km
import nebulosa.math.m
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import okio.ByteString.Companion.decodeBase64
import java.time.Duration
import java.time.LocalDateTime

class HorizonsServiceTest : StringSpec() {

    init {
        val service = HorizonsService()

        fun observe(command: String) = service
            .observer(
                command,
                138.73119026648095.deg, 35.36276754848444.deg, 3776.m,
                LocalDateTime.of(2022, 12, 25, 22, 0, 0),
                LocalDateTime.of(2022, 12, 25, 23, 0, 0),
                extraPrecision = true,
            ).execute()
            .body()
            .shouldNotBeNull()
            .also { it.shouldNotBeEmpty() }

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
        "observer: sun" {
            val ephemeris = observe("10")
            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "274.11210"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "-23.38427"
        }
        "observer: moon" {
            val ephemeris = observe("301")
            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "313.69977"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "-23.34363"
        }
        "observer: mars" {
            val ephemeris = observe("499")
            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "67.87667"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "24.65746"
            ephemeris[dateTime]!![HorizonsQuantity.CONSTELLATION] shouldBe "Tau"
            ephemeris[dateTime]!![HorizonsQuantity.ILLUMINATED_FRACTION] shouldBe "98.32712"
            ephemeris[dateTime]!![HorizonsQuantity.VISUAL_MAGNITUDE] shouldBe "-1.426"
            ephemeris[dateTime]!![HorizonsQuantity.SURFACE_BRIGHTNESS] shouldBe "4.239"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_AZ] shouldStartWith "317.88982"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_ALT] shouldStartWith "-16.31968"
            ephemeris[dateTime]!![HorizonsQuantity.APPARENT_HOUR_ANGLE] shouldStartWith "8.99305"
        }
        "observer: ceres by SPK ID" {
            val ephemeris = observe("DES=2000001;")
            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "185.92892"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "9.90348"
        }
        "observer: ceres by IAU Number" {
            val ephemeris = observe("1;")
            val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "185.92892"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "9.90348"
        }
        "observer: osculating elements" {
            val ephemeris = service
                .observerWithOsculationElements(
                    "(2023 GA2)", "2460049.5", ".6183399929327511",
                    "30.04427847488657", "30.56835826458952", "19.84449491210952",
                    ".3107780828530178", "2459989.479453452084",
                    longitude = 314.4173.deg, latitude = (-22.5354318).deg, elevation = 1.81754.km,
                    startTime = LocalDateTime.of(2023, 3, 11, 0, 0, 0),
                    endTime = LocalDateTime.of(2023, 4, 11, 0, 0, 0),
                    extraPrecision = true,
                    stepSize = Duration.ofDays(1L),
                ).execute()
                .body().shouldNotBeNull()

            ephemeris.shouldNotBeEmpty()

            val dateTime = LocalDateTime.of(2023, 3, 11, 0, 0, 0)
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "344.45591"
            ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "14.43086"
        }
    }
}
