import SatelliteEntityRepositoryTest.Companion.ISS_TLE
import SatelliteEntityRepositoryTest.Companion.save
import SkyObjectEntityRepositoryTest.Companion.save
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.api.atlas.EarthSeason
import nebulosa.api.atlas.EarthSeasonFinder
import nebulosa.api.atlas.Location
import nebulosa.api.atlas.MoonPhase
import nebulosa.api.atlas.MoonPhaseFinder
import nebulosa.api.atlas.SatelliteGroupType
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.atlas.SkyAtlasService
import nebulosa.api.atlas.SkyObjectEntityRepository
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.horizons.HorizonsService
import nebulosa.math.deg
import nebulosa.math.formatDMS
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.math.hours
import nebulosa.math.m
import nebulosa.math.toDegrees
import nebulosa.nova.astrometry.Constellation
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.skycatalog.SkyObjectType
import nebulosa.test.HTTP_CLIENT
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.Executors

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

    @Test
    fun findMoonPhases() {
        // Moon Phases.
        // https://www.timeanddate.com/moon/phases/
        // https://aa.usno.navy.mil/calculated/moon/phases?date=2024-08-01&nump=50&format=p&submit=Get+Data
        val phases = MOON_PHASE_FINDER.find(LocalDate.of(2024, 8, 21))

        phases shouldHaveSize 4

        phases[0].dateTime.toString() shouldBe "2024-08-04T11:13"
        phases[0].name shouldBe MoonPhase.NEW_MOON

        phases[1].dateTime.toString() shouldBe "2024-08-12T15:18"
        phases[1].name shouldBe MoonPhase.FIRST_QUARTER

        phases[2].dateTime.toString() shouldBe "2024-08-19T18:25"
        phases[2].name shouldBe MoonPhase.FULL_MOON

        phases[3].dateTime.toString() shouldBe "2024-08-26T09:25"
        phases[3].name shouldBe MoonPhase.LAST_QUARTER
    }

    @Test
    fun findMoonPhasesWithTwoNewMoons() {
        val phases = MOON_PHASE_FINDER.find(LocalDate.of(2024, 12, 1))

        phases shouldHaveSize 5

        phases[0].dateTime.toString() shouldBe "2024-12-01T06:21"
        phases[0].name shouldBe MoonPhase.NEW_MOON

        phases[1].dateTime.toString() shouldBe "2024-12-08T15:26"
        phases[1].name shouldBe MoonPhase.FIRST_QUARTER

        phases[2].dateTime.toString() shouldBe "2024-12-15T09:01"
        phases[2].name shouldBe MoonPhase.FULL_MOON

        phases[3].dateTime.toString() shouldBe "2024-12-22T22:18"
        phases[3].name shouldBe MoonPhase.LAST_QUARTER

        phases[4].dateTime.toString() shouldBe "2024-12-30T22:26"
        phases[4].name shouldBe MoonPhase.NEW_MOON
    }

    @Test
    fun findMoonPhasesWithBlueMoon() {
        // https://aa.usno.navy.mil/calculated/moon/phases?date=2023-08-01&nump=50&format=p&submit=Get+Data
        val phases = MOON_PHASE_FINDER.find(LocalDate.of(2023, 8, 1))

        phases shouldHaveSize 5

        phases[0].dateTime.toString() shouldBe "2023-08-01T18:31"
        phases[0].name shouldBe MoonPhase.FULL_MOON

        phases[1].dateTime.toString() shouldBe "2023-08-08T10:28"
        phases[1].name shouldBe MoonPhase.LAST_QUARTER

        phases[2].dateTime.toString() shouldBe "2023-08-16T09:38"
        phases[2].name shouldBe MoonPhase.NEW_MOON

        phases[3].dateTime.toString() shouldBe "2023-08-24T09:57"
        phases[3].name shouldBe MoonPhase.FIRST_QUARTER

        phases[4].dateTime.toString() shouldBe "2023-08-31T01:35"
        phases[4].name shouldBe MoonPhase.FULL_MOON
    }

    @Test
    fun findMoonPhasesForPrecisionObservations() {
        val phases = MOON_PHASE_FINDER.find(LocalDate.of(2024, 8, 21), (-45.899).deg, (-23.219).deg, 890.0.m, -180L)

        phases shouldHaveSize 4

        phases[0].dateTime.toString() shouldBe "2024-08-04T06:45"
        phases[0].name shouldBe MoonPhase.NEW_MOON

        phases[1].dateTime.toString() shouldBe "2024-08-12T10:51"
        phases[1].name shouldBe MoonPhase.FIRST_QUARTER

        phases[2].dateTime.toString() shouldBe "2024-08-19T14:17"
        phases[2].name shouldBe MoonPhase.FULL_MOON

        phases[3].dateTime.toString() shouldBe "2024-08-26T06:28"
        phases[3].name shouldBe MoonPhase.LAST_QUARTER
    }

    @Test
    fun findEarthSeasons() {
        // https://www.weather.gov/media/ind/seasons.pdf
        val seasons = EARTH_SEASON_FINDER.find(2024, -180)

        seasons shouldHaveSize 4

        seasons[0].dateTime.toString() shouldBe "2024-03-20T00:06"
        seasons[0].name shouldBe EarthSeason.MARCH_EQUINOX

        seasons[1].dateTime.toString() shouldBe "2024-06-20T17:51"
        seasons[1].name shouldBe EarthSeason.JUNE_SOLSTICE

        seasons[2].dateTime.toString() shouldBe "2024-09-22T09:43"
        seasons[2].name shouldBe EarthSeason.SEPTEMBER_EQUINOX

        seasons[3].dateTime.toString() shouldBe "2024-12-21T06:20"
        seasons[3].name shouldBe EarthSeason.DECEMBER_SOLSTICE
    }

    companion object {

        private const val DATASOURCE = "jdbc:h2:mem:skyatlas;DB_CLOSE_DELAY=-1"

        private val CONNECTION = Database.connect(DATASOURCE, user = "root", password = "")

        @AfterAll
        @JvmStatic
        fun closeConnection() {
            TransactionManager.closeAndUnregister(CONNECTION)
        }

        init {
            SkyDatabaseMigrator(DATASOURCE).run()
        }

        private val THREAD_POOL_TASK_EXECUTOR = Executors.newSingleThreadExecutor()
        private val HORIZONS_SERVICE = HorizonsService(httpClient = HTTP_CLIENT)
        private val HORIZONS_EPHEMERIS_PROVIDER = HorizonsEphemerisProvider(HORIZONS_SERVICE)
        private val BODY_EPHEMERIS_PROVIDER = BodyEphemerisProvider(THREAD_POOL_TASK_EXECUTOR)
        private val SMALL_BODY_DATABASE_SERVICE = SmallBodyDatabaseService()
        private val MOON_PHASE_FINDER = MoonPhaseFinder(HORIZONS_SERVICE)
        private val EARTH_SEASON_FINDER = EarthSeasonFinder(HORIZONS_SERVICE)

        private val OBJECT_MAPPER = jsonMapper {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }

        private val SIMBAD_ENTITY_REPOSITORY = SkyObjectEntityRepository(CONNECTION).apply {
            save("Sirius", SkyObjectType.STAR, Constellation.CMA, -1.45, "06 45 06".hours, "-16 43 33".deg)
            save("75 Tucanae", SkyObjectType.GLOBULAR_CLUSTER, Constellation.TUC, 6.58, "01 03 12".hours, "-70 50 39".deg)
        }

        private val SATELLITE_REPOSITORY = SatelliteRepository(CONNECTION).apply {
            save("ISS (ZARYA)", ISS_TLE, SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
        }

        private val SERVICE = SkyAtlasService(
            HORIZONS_EPHEMERIS_PROVIDER, BODY_EPHEMERIS_PROVIDER, SMALL_BODY_DATABASE_SERVICE,
            SATELLITE_REPOSITORY, SIMBAD_ENTITY_REPOSITORY, HTTP_CLIENT, OBJECT_MAPPER,
            MOON_PHASE_FINDER, EARTH_SEASON_FINDER, Executors.newSingleThreadScheduledExecutor(),
        )

        private val LOCATION = Location("-19.846616".deg, "-43.96872".deg, 852.0.m, -180)
        private val DATE_TIME = LocalDateTime.of(2024, 6, 30, 9, 50, 0)
    }
}
