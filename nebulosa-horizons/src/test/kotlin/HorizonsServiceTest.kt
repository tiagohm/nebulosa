import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import nebulosa.horizons.*
import nebulosa.io.source
import nebulosa.math.deg
import nebulosa.math.km
import nebulosa.math.m
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.NAIF
import nebulosa.nasa.spk.Spk
import nebulosa.test.HTTP_CLIENT
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HorizonsServiceTest {

    private fun observe(
        command: String,
        startDate: LocalDateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0),
    ) = SERVICE.observer(
        command, ObservingSite.Geographic(138.73119026648095.deg, 35.36276754848444.deg, 3776.m),
        startDate, startDate.plusDays(1L), extraPrecision = true,
    ).execute().body().shouldNotBeNull().also { it.shouldNotBeEmpty() }

    @Test
    fun spk() {
        val start = LocalDateTime.of(2023, 1, 1, 0, 0)
        val end = LocalDateTime.of(2023, 12, 31, 23, 59)
        val spkId = NAIF.extendedPermanentAsteroidNumber(3517)
        val spkFile = SERVICE.spk(spkId, start, end).execute().body().shouldNotBeNull()
        spkFile.id shouldBeExactly spkId
        val spkBytes = spkFile.spk.decodeBase64()
        val spk = Spk(SourceDaf(spkBytes!!.asByteBuffer().source()))
        spk.shouldHaveSize(1)
        spk[NAIF.SUN, spkId].shouldNotBeNull()
    }

    @Test
    fun observerSun() {
        val ephemeris = observe("10")
        val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "274.11210"
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "-23.38427"
    }

    @Test
    fun observerMoon() {
        val ephemeris = observe("301")
        val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "313.69977"
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "-23.34363"
    }

    @Test
    fun observerMars() {
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

    @Test
    fun observerCeresBySpkId() {
        val ephemeris = observe("DES=2000001;")
        val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "185.92892"
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "9.90348"
    }

    @Test
    fun observerCeresByIauNumber() {
        val ephemeris = observe("1;")
        val dateTime = LocalDateTime.of(2022, 12, 25, 22, 0, 0)
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "185.92892"
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "9.90348"
    }

    @Test
    fun observerOsculatingElements() {
        val ephemeris = SERVICE
            .observerWithOsculationElements(
                "(2023 GA2)", "2460049.5", ".6183399929327511",
                "30.04427847488657", "30.56835826458952", "19.84449491210952",
                ".3107780828530178", "2459989.479453452084",
                longitude = 314.4173.deg, latitude = (-22.5354318).deg, elevation = 1.81754.km,
                startTime = LocalDateTime.of(2023, 3, 11, 0, 0, 0),
                endTime = LocalDateTime.of(2023, 4, 11, 0, 0, 0),
                extraPrecision = true,
                stepSizeInMinutes = 1,
            ).execute().body().shouldNotBeNull()

        ephemeris.shouldNotBeEmpty()

        val dateTime = LocalDateTime.of(2023, 3, 11, 0, 0, 0)
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_RA] shouldStartWith "344.45591"
        ephemeris[dateTime]!![HorizonsQuantity.ASTROMETRIC_DEC] shouldStartWith "14.43086"
    }

    @Test
    fun capAndNofrag() {
        observe("DES=1000041;CAP;NOFRAG", LocalDateTime.now().minusDays(2L))
    }

    @Test
    fun nonUniqueObject() {
        shouldThrow<NonUniqueObjectException> { observe("DES=1000041;") }.recordItems.shouldNotBeEmpty()
    }

    @Test
    fun noMatchesFound() {
        shouldThrow<NoMatchesFoundException> { observe("DES=1;CAP;NOFRAG") }
    }

    companion object {

        @JvmStatic private val SERVICE = HorizonsService(httpClient = HTTP_CLIENT)
    }
}
