import SatelliteEntityRepositoryTest.Companion.ISS_TLE
import SatelliteEntityRepositoryTest.Companion.save
import SimbadEntityRepositoryTest.Companion.save
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.*
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.api.database.MyObjectBox
import nebulosa.horizons.HorizonsService
import nebulosa.math.*
import nebulosa.nova.astrometry.Constellation
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.skycatalog.SkyObjectType
import nebulosa.test.HTTP_CLIENT
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.LocalDateTime
import java.util.*

class SkyAtlasServiceTest {

    @Test
    fun objectTypes() {
        SERVICE.objectTypes.shouldHaveSize(2).shouldContainAll(SkyObjectType.STAR, SkyObjectType.GLOBULAR_CLUSTER)
    }

    @Test
    fun positionOfSun() {
        val position = SERVICE.positionOfSun(LOCATION, DATE_TIME, false)
        position.rightAscensionJ2000.formatHMS() shouldBe "06h38m40.2s"
        position.declinationJ2000.formatSignedDMS() shouldBe "+023°08'24.6\""
        position.rightAscension.formatHMS() shouldBe "06h40m07.7s"
        position.declination.formatSignedDMS() shouldBe "+023°07'10.5\""
        position.azimuth.formatDMS() shouldBe "037°55'12.8\""
        position.altitude.formatSignedDMS() shouldBe "+036°39'01.4\""
        position.magnitude shouldBeExactly -26.706
        position.constellation shouldBe Constellation.GEM
        position.distance shouldBe (1.017 plusOrMinus 1e-3)
        position.distanceUnit shouldBe "AU"
        position.illuminated shouldBeExactly 100.0
        position.elongation.toDegrees shouldBeExactly 0.0
    }

    @Test
    fun positionOfMoon() {
        val position = SERVICE.positionOfMoon(LOCATION, DATE_TIME, false)
        position.rightAscensionJ2000.formatHMS() shouldBe "01h47m21.7s"
        position.declinationJ2000.formatSignedDMS() shouldBe "+013°32'49.4\""
        position.rightAscension.formatHMS() shouldBe "01h48m39.8s"
        position.declination.formatSignedDMS() shouldBe "+013°40'06.4\""
        position.azimuth.formatDMS() shouldBe "306°54'06.0\""
        position.altitude.formatSignedDMS() shouldBe "+037°58'29.7\""
        position.magnitude shouldBeExactly -9.302
        position.constellation shouldBe Constellation.ARI
        position.distance shouldBe (367798.938 plusOrMinus 1e-3)
        position.distanceUnit shouldBe "km"
        position.illuminated shouldBe (32.301 plusOrMinus 1e-3)
        position.elongation.toDegrees shouldBe (69.142 plusOrMinus 1e-3)
    }

    @Test
    fun positionOfJupiter() {
        val position = SERVICE.positionOfPlanet(LOCATION, "599", DATE_TIME, false)
        position.rightAscensionJ2000.formatHMS() shouldBe "04h24m28.8s"
        position.declinationJ2000.formatSignedDMS() shouldBe "+020°55'01.7\""
        position.rightAscension.formatHMS() shouldBe "04h25m54.0s"
        position.declination.formatSignedDMS() shouldBe "+020°58'23.7\""
        position.azimuth.formatDMS() shouldBe "358°22'34.0\""
        position.altitude.formatSignedDMS() shouldBe "+049°09'53.9\""
        position.magnitude shouldBeExactly -2.025
        position.constellation shouldBe Constellation.TAU
        position.distance shouldBe (5.870 plusOrMinus 1e-3)
        position.distanceUnit shouldBe "AU"
        position.illuminated shouldBe (99.726 plusOrMinus 1e-3)
        position.elongation.toDegrees shouldBe (31.110 plusOrMinus 1e-3)
    }

    @Test
    fun positionOfApophis() {
        val apophis = SERVICE.searchMinorPlanet("apophis").spkId
        val position = SERVICE.positionOfPlanet(LOCATION, "DES=$apophis;", DATE_TIME, false)
        position.rightAscensionJ2000.formatHMS() shouldBe "06h33m49.8s"
        position.declinationJ2000.formatSignedDMS() shouldBe "+021°37'20.6\""
        position.rightAscension.formatHMS() shouldBe "06h35m16.4s"
        position.declination.formatSignedDMS() shouldBe "+021°36'16.5\""
        position.azimuth.formatDMS() shouldBe "038°00'41.8\""
        position.altitude.formatSignedDMS() shouldBe "+038°32'04.2\""
        position.magnitude shouldBeExactly 20.825
        position.constellation shouldBe Constellation.GEM
        position.distance shouldBe (2.018 plusOrMinus 1e-3)
        position.distanceUnit shouldBe "AU"
        position.illuminated shouldBe (99.972 plusOrMinus 1e-3)
        position.elongation.toDegrees shouldBe (1.885 plusOrMinus 1e-3)
    }

    @Test
    fun positionOfSirius() {
        val sirius = SERVICE.searchSkyObject("Sirius").shouldNotBeEmpty().first().id
        val position = SERVICE.positionOfSkyObject(LOCATION, sirius, DATE_TIME)
        position.rightAscensionJ2000.formatHMS() shouldBe "06h45m06.0s"
        position.declinationJ2000.formatSignedDMS() shouldBe "-016°43'33.0\""
        position.rightAscension.formatHMS() shouldBe "06h46m11.5s"
        position.declination.formatSignedDMS() shouldBe "-016°45'01.6\""
        position.azimuth.formatDMS() shouldBe "090°08'45.7\""
        position.altitude.formatSignedDMS() shouldBe "+057°41'06.5\""
        position.magnitude shouldBeExactly -1.45
        position.constellation shouldBe Constellation.CMA
    }

    @Test
    fun positionOfIss() {
        val iss = SERVICE.searchSatellites("ISS", emptyList()).shouldNotBeEmpty().first()
        val position = SERVICE.positionOfSatellite(LOCATION, iss, DATE_TIME)
        position.rightAscensionJ2000.formatHMS() shouldBe "14h47m37.8s"
        position.declinationJ2000.formatSignedDMS() shouldBe "-017°22'47.2\""
        position.rightAscension.formatHMS() shouldBe "14h49m00.4s"
        position.declination.formatSignedDMS() shouldBe "-017°29'00.1\""
        position.azimuth.formatDMS() shouldBe "144°36'58.9\""
        position.altitude.formatSignedDMS() shouldBe "-045°07'44.9\""
        position.constellation shouldBe Constellation.LIB
        position.distance shouldBe (9633.950 plusOrMinus 1e-3)
        position.distanceUnit shouldBe "km"
        position.illuminated shouldBe (79.282 plusOrMinus 1e-3)
        position.elongation.toDegrees shouldBe (125.849 plusOrMinus 1e-3)
    }

    @Test
    fun closeApproaches() {
        val phas = SERVICE.closeApproachesForMinorPlanets(7, 10.0, DATE_TIME.toLocalDate())
        phas.shouldHaveAtLeastSize(2).map { it.name }.shouldContainAll("(2017 MB3)", "(2024 LH)")
    }

    companion object {

        @JvmStatic private val BOX_STORE = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        @AfterAll
        @JvmStatic
        fun closeBoxStore() {
            BOX_STORE.close()
        }

        @JvmStatic private val THREAD_POOL_TASK_EXECUTOR = ThreadPoolTaskExecutor().also { it.initialize() }
        @JvmStatic private val HORIZONS_SERVICE = HorizonsService(httpClient = HTTP_CLIENT)
        @JvmStatic private val HORIZONS_EPHEMERIS_PROVIDER = HorizonsEphemerisProvider(HORIZONS_SERVICE)
        @JvmStatic private val BODY_EPHEMERIS_PROVIDER = BodyEphemerisProvider(THREAD_POOL_TASK_EXECUTOR)
        @JvmStatic private val SMALL_BODY_DATABASE_SERVICE = SmallBodyDatabaseService()
        @JvmStatic private val SATELLITE_BOX = BOX_STORE.boxFor<SatelliteEntity>()
        @JvmStatic private val SIMBAD_BOX = BOX_STORE.boxFor<SimbadEntity>()

        @JvmStatic private val SIMBAD_ENTITY_REPOSITORY = SimbadEntityRepository(SIMBAD_BOX).apply {
            save("Sirius", SkyObjectType.STAR, Constellation.CMA, -1.45, "06 45 06".hours, "-16 43 33".deg)
            save("75 Tucanae", SkyObjectType.GLOBULAR_CLUSTER, Constellation.TUC, 6.58, "01 03 12".hours, "-70 50 39".deg)
        }

        @JvmStatic private val SATELLITE_REPOSITORY = SatelliteRepository(SATELLITE_BOX).apply {
            save("ISS (ZARYA)", ISS_TLE, SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
        }

        @JvmStatic private val SERVICE = SkyAtlasService(
            HORIZONS_EPHEMERIS_PROVIDER, BODY_EPHEMERIS_PROVIDER, SMALL_BODY_DATABASE_SERVICE,
            SATELLITE_REPOSITORY, SIMBAD_ENTITY_REPOSITORY, HTTP_CLIENT
        )

        @JvmStatic private val LOCATION = Location("-19.846616".deg, "-43.96872".deg, 852.0.m, -180)
        @JvmStatic private val DATE_TIME = LocalDateTime.of(2024, 6, 30, 9, 50, 0)
    }
}
