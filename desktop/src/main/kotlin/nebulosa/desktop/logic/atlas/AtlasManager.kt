package nebulosa.desktop.logic.atlas

import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import nebulosa.constants.AU_KM
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.desktop.helper.runBlockingIO
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.atlas.provider.catalog.CatalogProvider
import nebulosa.desktop.logic.atlas.provider.ephemeris.BodyEphemerisProvider
import nebulosa.desktop.logic.atlas.provider.ephemeris.HorizonsEphemerisProvider
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.framing.FramingView
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.pmod
import nebulosa.nova.almanac.DiscreteFunction
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.sbd.SmallBody
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.skycatalog.SkyObject
import nebulosa.time.UTC
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import retrofit2.await
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Component
@EnableScheduling
class AtlasManager(@Autowired internal val view: AtlasView) : AbstractManager() {

    private val ephemerisCache = hashMapOf<Any, HorizonsEphemeris?>()
    private val rtsCache = hashMapOf<Any, Triple<String, String, String>>()
    private val pointsCache = hashMapOf<HorizonsEphemeris, List<XYItem>>()
    private val starsCache = hashMapOf<Int, Body>()
    private val dsosCache = hashMapOf<Int, Body>()

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var bodyEphemerisProvider: BodyEphemerisProvider
    @Autowired private lateinit var horizonsEphemerisProvider: HorizonsEphemerisProvider
    @Autowired private lateinit var smallBodyDatabaseLookupService: SmallBodyDatabaseLookupService
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var framingView: FramingView
    @Autowired private lateinit var starCatalogProvider: CatalogProvider<*>
    @Autowired private lateinit var dsoCatalogProvider: CatalogProvider<*>

    @Volatile private var observer: GeographicPosition? = null
    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var planet: AtlasView.Planet? = null
    @Volatile private var minorPlanet: SmallBody? = null
    @Volatile private var star: AtlasView.Star? = null
    @Volatile private var dso: AtlasView.DSO? = null
    @Volatile private var bodyName = ""

    private val civilDusk = doubleArrayOf(0.0, 0.0)
    private val nauticalDusk = doubleArrayOf(0.0, 0.0)
    private val astronomicalDusk = doubleArrayOf(0.0, 0.0)
    private val night = doubleArrayOf(0.0, 0.0)
    private val astronomicalDawn = doubleArrayOf(0.0, 0.0)
    private val nauticalDawn = doubleArrayOf(0.0, 0.0)
    private val civilDawn = doubleArrayOf(0.0, 0.0)

    val mountProperty
        get() = equipmentManager.selectedMount

    val mount: Mount?
        get() = mountProperty.value

    val computing = SimpleBooleanProperty()

    fun initialize() {
        eventBus.register(this)

        launch {
            val longitude = mount?.longitude ?: preferences.double("atlas.longitude")?.rad ?: Angle.ZERO
            val latitude = mount?.latitude ?: preferences.double("atlas.latitude")?.rad ?: Angle.ZERO
            val elevation = mount?.elevation ?: preferences.double("atlas.elevation")?.au ?: Distance.ZERO

            observer = Geoid.IERS2010.latLon(longitude, latitude, elevation)
            updateTitle()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMountEvent(event: MountEvent) {
        if (event.device !== equipmentManager.selectedMount.value) return

        when (event) {
            is MountGeographicCoordinateChanged -> {
                preferences.double("atlas.longitude", event.device.longitude.value)
                preferences.double("atlas.latitude", event.device.latitude.value)
                preferences.double("atlas.elevation", event.device.elevation.value)

                ephemerisCache.clear()
                pointsCache.clear()

                launch {
                    observer = Geoid.IERS2010.latLon(event.device.longitude, event.device.latitude, event.device.elevation)
                    updateTitle()

                    computeSun()
                    computeTab()
                }
            }
        }
    }

    private suspend fun updateTitle() = withMain {
        view.title = "Atlas · LAT: %.04f° LNG: %.04f° ELEV: %.0fm".format(
            observer!!.latitude.degrees, observer!!.longitude.degrees, observer!!.elevation.meters
        )
    }

    suspend fun computeTab(type: AtlasView.TabType): HorizonsEphemeris? {
        if (!view.showing) return null

        LOG.info("computing tab. type={}", type)

        tabType = type

        return when (type) {
            AtlasView.TabType.SUN -> computeSun()
            AtlasView.TabType.MOON -> computeMoon()
            AtlasView.TabType.PLANET -> computePlanet()
            AtlasView.TabType.MINOR_PLANET -> computeMinorPlanet()
            AtlasView.TabType.STAR -> computeStar()
            AtlasView.TabType.DSO -> computeDSO()
        }
    }

    suspend fun computeTab(): HorizonsEphemeris? {
        return computeTab(tabType)
    }

    @Scheduled(cron = "0 * * * * *")
    private fun computeTabAtScheduledTime() = runBlockingIO {
        computeTab()
    }

    suspend fun populatePlanets() = withIO {
        val planets = listOf(
            AtlasView.Planet("Mercury", "Planet", "199"),
            AtlasView.Planet("Venus", "Planet", "299"),
            AtlasView.Planet("Mars", "Planet", "499"),
            AtlasView.Planet("Jupiter", "Planet", "599"),
            AtlasView.Planet("Saturn", "Planet", "699"),
            AtlasView.Planet("Uranus", "Planet", "799"),
            AtlasView.Planet("Neptune", "Planet", "899"),
            AtlasView.Planet("Pluto", "Dwarf Planet", "999"),
            AtlasView.Planet("Phobos", "Mars' Satellite", "401"),
            AtlasView.Planet("Deimos", "Mars' Satellite", "402"),
            AtlasView.Planet("Io", "Jupiter's Satellite", "501"),
            AtlasView.Planet("Europa", "Jupiter's Satellite", "402"),
            AtlasView.Planet("Ganymede", "Jupiter's Satellite", "403"),
            AtlasView.Planet("Callisto", "Jupiter's Satellite", "504"),
            AtlasView.Planet("Mimas", "Saturn's Satellite", "601"),
            AtlasView.Planet("Enceladus", "Saturn's Satellite", "602"),
            AtlasView.Planet("Tethys", "Saturn's Satellite", "603"),
            AtlasView.Planet("Dione", "Saturn's Satellite", "604"),
            AtlasView.Planet("Rhea", "Saturn's Satellite", "605"),
            AtlasView.Planet("Titan", "Saturn's Satellite", "606"),
            AtlasView.Planet("Hyperion", "Saturn's Satellite", "607"),
            AtlasView.Planet("Iapetus", "Saturn's Satellite", "608"),
            AtlasView.Planet("Ariel", "Uranus' Satellite", "701"),
            AtlasView.Planet("Umbriel", "Uranus' Satellite", "702"),
            AtlasView.Planet("Titania", "Uranus' Satellite", "703"),
            AtlasView.Planet("Oberon", "Uranus' Satellite", "704"),
            AtlasView.Planet("Miranda", "Uranus' Satellite", "705"),
            AtlasView.Planet("Triton", "Neptune's Satellite", "801"),
            AtlasView.Planet("Charon", "Pluto's Satellite", "901"),
            AtlasView.Planet("1 Ceres", "Dwarf Planet", "1;"),
            AtlasView.Planet("90377 Sedna", "Dwarf Planet", "90377;"),
            AtlasView.Planet("136199 Eris", "Dwarf Planet", "136199;"),
            AtlasView.Planet("2 Pallas", "Asteroid", "2;"),
            AtlasView.Planet("3 Juno", "Asteroid", "3;"),
            AtlasView.Planet("4 Vesta", "Asteroid", "4;"),
        )

        view.populatePlanet(planets)
    }

    suspend fun computeSun(): HorizonsEphemeris? {
        bodyName = "Sun"
        return SUN_TARGET.computeBody()
    }

    suspend fun computeMoon(show: Boolean = true): HorizonsEphemeris? {
        bodyName = "Moon"
        return MOON_TARGET.computeBody(show)
    }

    suspend fun computePlanet(body: AtlasView.Planet? = planet): HorizonsEphemeris? {
        planet = body ?: return null
        bodyName = body.name
        return body.command.computeBody()
    }

    suspend fun computeMinorPlanet(body: SmallBody? = minorPlanet): HorizonsEphemeris? {
        minorPlanet = body ?: return null
        bodyName = body.body!!.fullname
        return "DES=${body.body!!.spkId};".computeBody() ?: body.computeBody()
    }

    suspend fun computeStar(body: AtlasView.Star? = star): HorizonsEphemeris? {
        star = body ?: return null
        bodyName = body.skyObject.names.joinToString(", ")
        return starsCache.computeFixedStar(body.skyObject).computeBody()
    }

    suspend fun computeDSO(body: AtlasView.DSO? = dso): HorizonsEphemeris? {
        dso = body ?: return null
        bodyName = body.skyObject.names.joinToString(", ")
        return dsosCache.computeFixedStar(body.skyObject).computeBody()
    }

    private suspend fun String.computeBody(show: Boolean = true): HorizonsEphemeris? {
        return if (isNotEmpty()) computeAltitude(this, show = show) else null
    }

    private suspend fun Body.computeBody(show: Boolean = true): HorizonsEphemeris? {
        return computeAltitude(this, show = show)
    }

    private suspend fun SmallBody.computeBody(show: Boolean = true): HorizonsEphemeris? {
        return computeAltitude(this, show = show)
    }

    private fun HorizonsEphemeris.makePoints(): List<XYItem> {
        if (this in pointsCache) return pointsCache[this]!!

        val points = ArrayList<XYItem>(25 * 2)
        var x = 0.0

        times.forEachIndexed { i, time ->
            if (time.minute % 30 != 0) return@forEachIndexed
            val y = elements[i][HorizonsQuantity.APPARENT_ALT]!!.toDoubleOrNull() ?: 0.0
            points.add(XYChartItem(x, y))
            x += 0.5
        }

        pointsCache[this] = points

        return points
    }

    private fun computeTwilight(altitudes: DoubleArray, target: Any) {
        LOG.info("computing twilight. target={}", target)

        // Expected discrete values: [4, 3, 2, 1, 2, 3, 4, 0]
        val (a) = findDiscrete(0.0, 1440.0, TwilightDiscreteFunction(altitudes), 1.0)

        civilDusk[0] = a[0] / 60.0
        civilDusk[1] = a[1] / 60.0
        nauticalDusk[0] = a[1] / 60.0
        nauticalDusk[1] = a[2] / 60.0
        astronomicalDusk[0] = a[2] / 60.0
        astronomicalDusk[1] = a[3] / 60.0
        night[0] = a[3] / 60.0
        night[1] = a[4] / 60.0
        astronomicalDawn[0] = a[4] / 60.0
        astronomicalDawn[1] = a[5] / 60.0
        nauticalDawn[0] = a[5] / 60.0
        nauticalDawn[1] = a[6] / 60.0
        civilDawn[0] = a[6] / 60.0
        civilDawn[1] = a[7] / 60.0
    }

    private suspend fun HorizonsEphemeris.computeRTS(
        altitudes: DoubleArray,
        target: Any, force: Boolean, show: Boolean,
    ) = withIO {
        if (force || target !in rtsCache) {
            LOG.info("computing RTS. target={}", target)

            val (a, b) = findDiscrete(0.0, 1440.0, RisingAndSettingDiscreteFunction(altitudes), 1.0)

            val risingIndex = b.indexOf(1)
            val settingIndex = b.indexOf(0)
            val offset = OffsetDateTime.now().offset.totalSeconds.toLong()
            val settingTime = if (settingIndex >= 0) times[a[settingIndex].toInt()].plusSeconds(offset).format(RTS_FORMAT) else "-"
            val risingTime = if (risingIndex >= 0) times[a[risingIndex].toInt()].plusSeconds(offset).format(RTS_FORMAT) else "-"

            val maxAltitude = altitudes.max()
            val transitIndex = altitudes.indexOfFirst { it == maxAltitude }
            val transitTime = if (transitIndex >= 0) times[transitIndex].plusSeconds(offset).format(RTS_FORMAT) else "-"

            rtsCache[target] = Triple(risingTime, transitTime, settingTime)
        }

        if (show) {
            view.updateRTS(rtsCache[target]!!)
        }
    }

    private suspend fun HorizonsEphemeris.computeTwilightAndRTS(
        target: Any,
        force: Boolean, show: Boolean
    ) = withIO {
        LOG.info("computing twilight and RTS. target={}, force={}", target, force)
        val altitudes = DoubleArray(elements.size) { elements[it][HorizonsQuantity.APPARENT_ALT]!!.toDouble() }
        if (show && force && target == SUN_TARGET) computeTwilight(altitudes, target)
        computeRTS(altitudes, target, force, show)
    }

    private suspend fun drawAltitude(points: List<XYItem>) = withIO {
        val now = (LocalTime.now().toSecondOfDay() / 3600.0 - 12.0) pmod 24.0

        LOG.info("drawing altitude chart. now={}", now)

        view.drawAltitude(
            points, now,
            civilDawn, nauticalDawn, astronomicalDawn,
            civilDusk, nauticalDusk, astronomicalDusk,
            night,
        )
    }

    private suspend fun computeAltitude(
        target: Any,
        force: Boolean = false,
        show: Boolean = true,
    ) = withIO {
        val observer = observer ?: return@withIO null

        withMain { computing.set(true) }

        LOG.info("computing altitude. target={}, force={}, show={}", target, force, show)

        try {
            val prevEphemeris = ephemerisCache[target]

            val ephemeris = when (target) {
                is SmallBody -> horizonsEphemerisProvider.compute(target, observer, force)
                MOON_TARGET -> horizonsEphemerisProvider.compute(MOON_TARGET, observer, force)
                is String -> horizonsEphemerisProvider.compute(target, observer, force)
                is Body -> bodyEphemerisProvider.compute(target, observer, force)
                else -> null
            }

            if (ephemeris != null && !ephemeris.isEmpty()) {
                ephemerisCache[target] = ephemeris

                ephemeris.computeTwilightAndRTS(target, ephemeris !== prevEphemeris, show)
            }

            if (ephemeris == null) {
                view.clearAltitudeAndCoordinates()
                LOG.error("unable to retrieve ephemeris. target={}", target)
                null
            } else if (ephemeris.isEmpty()) {
                view.clearAltitudeAndCoordinates()
                LOG.warn("retrieved empty epheremis. target={}", target)
                null
            } else {
                LOG.info("ephemeris was retrieved. target={}, start={}, end={}", target, ephemeris.start, ephemeris.endInclusive)
                if (show) ephemeris.showBodyCoordinatesAndInfos(target)
                ephemeris
            }
        } catch (e: Throwable) {
            LOG.error("failed to retrieve ephemeris.", e)
            null
        } finally {
            withMain { computing.set(false) }
        }
    }

    private suspend fun HorizonsEphemeris.showBodyCoordinatesAndInfos(target: Any) {
        computeCoordinates(target)
        drawAltitude(makePoints())
    }

    private suspend fun HorizonsEphemeris.computeCoordinates(target: Any) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = this[now] ?: return

        LOG.info("computing coordinates. now={}, target={}, element={}", now, target, element)

        val extra = ArrayList<Pair<String, String>>(4)

        val lightTime = element[HorizonsQuantity.ONE_WAY_LIGHT_TIME]?.toDoubleOrNull() ?: 0.0
        val distance = lightTime * (SPEED_OF_LIGHT * 0.06) // km
        if (distance <= 0.0) extra.add("Distance" to "-")
        else if (distance >= AU_KM) extra.add("Distance (AU)" to "%.06f".format(distance / AU_KM))
        else extra.add("Distance (km)" to "%.03f".format(distance))

        val magnitude = element[HorizonsQuantity.VISUAL_MAGNITUDE]?.ifBlank { null }
        if (magnitude != null) extra.add("Magnitude" to magnitude)

        val illuminated = element[HorizonsQuantity.ILLUMINATED_FRACTION]?.ifBlank { null }
        if (illuminated != null) extra.add("Illuminated (%)" to illuminated)

        val elongation = element[HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE]?.split(",")?.first()
        if (elongation != null) extra.add("Elongation (deg)" to elongation)

        view.updateInfo(bodyName, extra)

        computeEquatorialCoordinates(element)
        computeHorizontalCoordinates(element)
    }

    private suspend fun computeEquatorialCoordinates(element: HorizonsElement) = withIO {
        val raJ2000 = element[HorizonsQuantity.ASTROMETRIC_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val decJ2000 = element[HorizonsQuantity.ASTROMETRIC_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val ra = element[HorizonsQuantity.APPARENT_RA]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val dec = element[HorizonsQuantity.APPARENT_DEC]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val epoch = UTC.now()
        val constellation = Constellation.find(ICRF.equatorial(ra, dec, time = epoch, epoch = epoch))
        view.updateEquatorialCoordinates(ra, dec, raJ2000, decJ2000, constellation)
    }

    private suspend fun computeHorizontalCoordinates(element: HorizonsElement) = withIO {
        val az = element[HorizonsQuantity.APPARENT_AZ]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        val alt = element[HorizonsQuantity.APPARENT_ALT]?.toDoubleOrNull()?.deg ?: Angle.ZERO
        view.updateHorizontalCoordinates(az, alt)
    }

    suspend fun updateSunImage() {
        if (!view.showing) return

        view.updateSunImage()
    }

    @Scheduled(cron = "0 */15 * * * *")
    private fun updateSunImageAtSchduledTime() {
        runBlocking(Dispatchers.IO) { updateSunImage() }
    }

    suspend fun updateMoonImage() {
        if (!view.showing) return

        val ephemeris = computeMoon(false)

        val now = LocalDateTime.now(ZoneOffset.UTC)
        val element = ephemeris?.get(now) ?: return
        val sot = element[HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE]!!.split(",")
        val angle = sot[0].toDouble()
        val leading = sot[1] == "/L"
        val phase = if (leading) 360.0 - angle else angle
        val age = 29.53058868 * (phase / 360.0)
        LOG.info("computed Moon phase. angle={}, age={}", phase, age)

        view.updateMoonImage(phase, age, Angle.ZERO)
    }

    @Scheduled(cron = "0 0 * * * *")
    private fun updateMoonImageAtSheduledTime() {
        runBlocking(Dispatchers.IO) { updateMoonImage() }
    }

    suspend fun searchMinorPlanet(text: String) = withIO {
        withMain { computing.set(true) }

        val smallBody = smallBodyDatabaseLookupService.search(text).await()

        if (!smallBody.message.isNullOrEmpty()) {
            view.showAlert(smallBody.message!!)
        } else if (!smallBody.list.isNullOrEmpty()) {
            view.showAlert("Found ${smallBody.list!!.size} record(s). Please refine your search criteria, and try again.")
        } else {
            val elements = ArrayList<AtlasView.MinorPlanet>(16)

            fun makeValue(value: String?, unit: String?) = buildString {
                if (!value.isNullOrEmpty()) append(value)
                if (!unit.isNullOrEmpty()) append(" ").append(unit)
            }

            smallBody.orbit!!.elements.map {
                elements.add(AtlasView.MinorPlanet(it.label, it.title, makeValue(it.value, it.units)))
            }

            smallBody.physical?.map {
                elements.add(AtlasView.MinorPlanet(it.name, it.title, makeValue(it.value, it.units)))
            }

            view.populateMinorPlanet(elements)

            computeMinorPlanet(smallBody)
        }

        withMain { computing.set(false) }
    }

    suspend fun searchStar(text: String) = withIO {
        val dsos = starCatalogProvider.searchBy(text)
        view.populateStar(dsos.map { AtlasView.Star(it) })
    }

    suspend fun searchDSO(text: String) = withIO {
        val dsos = dsoCatalogProvider.searchBy(text)
        view.populateDSOs(dsos.map { AtlasView.DSO(it) })
    }

    private fun trackModeForCurrentTab() {
        val mount = mount ?: return

        when (tabType) {
            AtlasView.TabType.SUN -> mount.trackingMode(TrackMode.SOLAR)
            AtlasView.TabType.MOON -> mount.trackingMode(TrackMode.LUNAR)
            else -> if (TrackMode.KING in mount.trackModes) mount.trackingMode(TrackMode.KING)
            else mount.trackingMode(TrackMode.SIDEREAL)
        }
    }

    fun goTo(ra: Angle, dec: Angle) {
        trackModeForCurrentTab()
        mount?.goTo(ra, dec)
    }

    fun slewTo(ra: Angle, dec: Angle) {
        trackModeForCurrentTab()
        mount?.slewTo(ra, dec)
    }

    fun sync(ra: Angle, dec: Angle) {
        mount?.sync(ra, dec)
    }

    suspend fun frame(ra: Angle, dec: Angle) = withMain {
        framingView.show(requestFocus = true)
        framingView.load(ra, dec)
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferences.double("atlas.screen.x", max(0.0, view.x))
        preferences.double("atlas.screen.y", max(0.0, view.y))
    }

    fun loadPreferences() {
        preferences.double("atlas.screen.x")?.also { view.x = it }
        preferences.double("atlas.screen.y")?.also { view.y = it }
    }

    override fun close() {
        super.close()

        savePreferences()

        eventBus.unregister(this)
    }

    private class TwilightDiscreteFunction(private val altitudes: DoubleArray) : DiscreteFunction {

        override val stepSize = 1.0

        override fun compute(x: Double): Int {
            val index = x.toInt()
            val altitude = altitudes[index]

            return when {
                altitude <= ASTRONOMICAL_TWILIGHT -> 1 // Night.
                altitude <= NAUTICAL_TWILIGHT -> 2 // Astronomical.
                altitude <= CIVIL_TWILIGHT -> 3 // Nautical.
                altitude < 0.0 -> 4 // Civil.
                else -> 0
            }
        }
    }

    private class RisingAndSettingDiscreteFunction(private val altitudes: DoubleArray) : DiscreteFunction {

        override val stepSize = 1.0

        override fun compute(x: Double): Int {
            val index = x.toInt()
            return if (altitudes[index] >= 0.0) 1 else 0
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AtlasManager::class.java)
        @JvmStatic private val RTS_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

        private const val SUN_TARGET = "10"
        private const val MOON_TARGET = "301"

        private const val ASTRONOMICAL_TWILIGHT = -18.0
        private const val NAUTICAL_TWILIGHT = -12.0
        private const val CIVIL_TWILIGHT = -6.0

        @JvmStatic
        private fun MutableMap<Int, Body>.computeFixedStar(body: SkyObject): Body {
            return if (body.id !in this) {
                val star = FixedStar(
                    body.rightAscension, body.declination,
                    body.pmRA, body.pmDEC, body.parallax, body.radialVelocity,
                )
                this[body.id] = star
                star
            } else {
                this[body.id]!!
            }
        }
    }
}
