package nebulosa.desktop.logic.atlas

import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.data.XYItem
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import nebulosa.constants.AU_KM
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.desktop.helper.await
import nebulosa.desktop.helper.runBlockingIO
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.atlas.provider.ephemeris.BodyEphemerisProvider
import nebulosa.desktop.logic.atlas.provider.ephemeris.HorizonsEphemerisProvider
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.service.SkyObjectService
import nebulosa.desktop.view.atlas.AtlasView
import nebulosa.desktop.view.framing.FramingView
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsEphemeris
import nebulosa.horizons.HorizonsQuantity
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.indi.device.mount.TrackMode
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.nova.almanac.DiscreteFunction
import nebulosa.nova.almanac.findDiscrete
import nebulosa.nova.astrometry.*
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.sbd.SmallBody
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.skycatalog.AxisSize
import nebulosa.skycatalog.SkyObject
import nebulosa.time.UTC
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import retrofit2.await
import java.net.SocketException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
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
    @Autowired private lateinit var skyObjectService: SkyObjectService
    @Autowired private lateinit var okHttpClient: OkHttpClient

    @Volatile private var tabType = AtlasView.TabType.SUN
    @Volatile private var planet: AtlasView.Planet? = null
    @Volatile private var minorPlanet: SmallBody? = null
    @Volatile private var star: SkyObject? = null
    @Volatile private var dso: SkyObject? = null
    @Volatile private var bodyName = ""
    @Volatile private var starFilter = SkyObjectService.Filter.EMPTY
    @Volatile private var dsoFilter = SkyObjectService.Filter.EMPTY

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

    val observer = SimpleObjectProperty<GeographicPosition>()
    val computing = SimpleBooleanProperty()

    fun initialize() {
        eventBus.register(this)
    }

    fun loadCoordinatesFromMount() {
        val longitude = mount?.longitude ?: preferenceService.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = mount?.latitude ?: preferenceService.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = mount?.elevation ?: preferenceService.double("atlas.elevation")?.au ?: Distance.ZERO

        loadCoordinates(latitude, longitude, elevation)
    }

    fun loadCoordinates(latitude: Angle, longitude: Angle, elevation: Distance = Distance.ZERO, fromMount: Boolean = false) {
        observer.set(Geoid.IERS2010.latLon(longitude, latitude, elevation))

        LOG.info("using coordinates. latitude={}, longitude={}, elevation={}", latitude.degrees, longitude.degrees, elevation.meters)

        preferenceService.double("atlas.longitude", longitude.value)
        preferenceService.double("atlas.latitude", latitude.value)
        preferenceService.double("atlas.elevation", elevation.value)
    }

    private fun clearCache() {
        ephemerisCache.clear()
        pointsCache.clear()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        if (event.device !== mount) return

        when (event) {
            is MountGeographicCoordinateChanged -> {
                val useCoordinatesFromMount = preferenceService.bool("atlas.useCoordinatesFromMount")

                if (useCoordinatesFromMount) {
                    clearCache()

                    launch {
                        withMain { loadCoordinatesFromMount() }

                        computeSun(tabType == AtlasView.TabType.SUN)

                        if (tabType != AtlasView.TabType.SUN) {
                            computeTab()
                        }
                    }
                }
            }
        }
    }

    suspend fun computeTab(type: AtlasView.TabType): HorizonsEphemeris? {
        if (!view.showing) return null

        LOG.info("computing tab. type={}", type)

        val ephemeris = when (type) {
            AtlasView.TabType.SUN -> computeSun()
            AtlasView.TabType.MOON -> computeMoon()
            AtlasView.TabType.PLANET -> computePlanet()
            AtlasView.TabType.MINOR_PLANET -> computeMinorPlanet()
            AtlasView.TabType.STAR -> computeStar()
            AtlasView.TabType.DSO -> computeDSO()
        }

        if (ephemeris != null) {
            tabType = type
        }

        return ephemeris
    }

    suspend fun computeTab(): HorizonsEphemeris? {
        return computeTab(tabType)
    }

    @Scheduled(cron = "0 * * * * *")
    private fun computeTabAtScheduledTime() = runBlockingIO {
        if (view.initialized && !view.manualMode) {
            computeTab()
        }
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

    suspend fun computeBody(type: AtlasView.TabType, body: Any? = null): HorizonsEphemeris? {
        val ephemeris = if (type == AtlasView.TabType.SUN) computeSun()
        else if (type == AtlasView.TabType.MOON) computeMoon()
        else if (type == AtlasView.TabType.PLANET && body is AtlasView.Planet) computePlanet(body)
        else if (type == AtlasView.TabType.MINOR_PLANET && body is SmallBody) computeMinorPlanet(body)
        else if (type == AtlasView.TabType.STAR && body is SkyObject) computeStar(body)
        else if (type == AtlasView.TabType.DSO && body is SkyObject) computeDSO(body)
        else null

        if (ephemeris != null) {
            tabType = type
        }

        return ephemeris
    }

    private suspend fun computeSun(show: Boolean = true): HorizonsEphemeris? {
        bodyName = "Sun"
        return SUN_TARGET.computeBody(show)
    }

    private suspend fun computeMoon(show: Boolean = true): HorizonsEphemeris? {
        bodyName = "Moon"
        return MOON_TARGET.computeBody(show)
    }

    private suspend fun computePlanet(body: AtlasView.Planet? = planet): HorizonsEphemeris? {
        planet = body ?: return null
        bodyName = body.name
        return body.command.computeBody()
    }

    private suspend fun computeMinorPlanet(body: SmallBody? = minorPlanet): HorizonsEphemeris? {
        minorPlanet = body ?: return null
        bodyName = body.body!!.fullname
        return "DES=${body.body!!.spkId};".computeBody() ?: body.computeBody()
    }

    private suspend fun computeStar(body: SkyObject? = star): HorizonsEphemeris? {
        star = body ?: return null
        bodyName = body.names
        return starsCache.computeFixedStar(body).computeBody(body = star)
    }

    private suspend fun computeDSO(body: SkyObject? = dso): HorizonsEphemeris? {
        dso = body ?: return null
        bodyName = body.names
        return dsosCache.computeFixedStar(body).computeBody(body = dso)
    }

    private suspend fun String.computeBody(show: Boolean = true): HorizonsEphemeris? {
        return if (isNotEmpty()) computeAltitude(this, show = show) else null
    }

    private suspend fun Body.computeBody(
        show: Boolean = true,
        body: SkyObject? = null,
    ): HorizonsEphemeris? {
        return computeAltitude(this, show = show, body = body)
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
            points.add(XYChartItem(x, y, "", "%02d:%02d · %.0f°".format((12 + x.toInt()) % 24, time.minute, y)))
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
            val offset = view.timeOffset.totalSeconds.toLong()
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
        force: Boolean, show: Boolean,
    ) = withIO {
        LOG.info("computing twilight and RTS. target={}, force={}", target, force)
        val altitudes = DoubleArray(elements.size) { elements[it][HorizonsQuantity.APPARENT_ALT]!!.toDouble() }
        if (force && target == SUN_TARGET) computeTwilight(altitudes, target)
        computeRTS(altitudes, target, force, show)
    }

    private suspend fun drawAltitude(points: List<XYItem>) {
        view.drawPoints(points)
    }

    private suspend fun drawNow() {
        view.drawNow()
    }

    private suspend fun drawTwilight() {
        view.drawTwilight(
            civilDawn, nauticalDawn, astronomicalDawn,
            civilDusk, nauticalDusk, astronomicalDusk,
            night,
        )
    }

    private suspend fun computeAltitude(
        target: Any,
        force: Boolean = false,
        show: Boolean = true,
        body: SkyObject? = null,
        fallback: Boolean = false,
    ): HorizonsEphemeris? = withIO {
        val observer = observer.get() ?: return@withIO null

        withMain { computing.set(true) }

        LOG.info("computing altitude. target={}, force={}, show={}, fallback={}", target, force, show, fallback)

        try {
            val prevEphemeris = ephemerisCache[target]

            val ephemeris = when (target) {
                is SmallBody -> horizonsEphemerisProvider.compute(target, observer, view, force)
                MOON_TARGET -> if (fallback) bodyEphemerisProvider.compute(MOON_BODY, observer, view, force)
                else horizonsEphemerisProvider.compute(MOON_TARGET, observer, view, force)
                is String -> if (fallback) PLANET_BODIES[target]?.let { bodyEphemerisProvider.compute(it, observer, view, force) }
                else horizonsEphemerisProvider.compute(target, observer, view, force)
                is Body -> bodyEphemerisProvider.compute(target, observer, view, force)
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
                if (show) ephemeris.showBodyCoordinatesAndInfos(target, body)
                ephemeris
            }
        } catch (e: SocketException) {
            if (!fallback) {
                computeAltitude(target, force, show, body, true)
            } else {
                LOG.error("failed to retrieve ephemeris.", e)
                null
            }
        } catch (e: Throwable) {
            LOG.error("failed to retrieve ephemeris.", e)
            null
        } finally {
            withMain { computing.set(false) }
        }
    }

    private suspend fun HorizonsEphemeris.showBodyCoordinatesAndInfos(target: Any, body: SkyObject?) {
        drawNow()
        computeCoordinates(target, body)
        drawAltitude(makePoints())
        drawTwilight()
    }

    private suspend fun HorizonsEphemeris.computeCoordinates(target: Any, body: SkyObject?) {
        val timeOffset = view.timeOffset
        val date = if (LocalTime.now(timeOffset).hour < 12) if (view.time.hour >= 12) view.date.minusDays(1L) else view.date
        else if (view.time.hour < 12) view.date.plusDays(1L)
        else view.date

        val now = LocalDateTime.of(date, view.time).minusSeconds(timeOffset.totalSeconds.toLong())
        val element = this[now] ?: return LOG.warn("ephemeris not found. now={}", now)

        LOG.info("computing coordinates. now={}, target={}, element={}", now, target, element)

        val extra = ArrayList<Pair<String, String>>(4)

        if (body == null) {
            val lightTime = element[HorizonsQuantity.ONE_WAY_LIGHT_TIME]?.toDoubleOrNull() ?: 0.0
            val distance = lightTime * (SPEED_OF_LIGHT * 0.06) // km
            if (distance <= 0.0) extra.add("Distance" to "-")
            else if (distance >= AU_KM) extra.add("Distance (AU)" to "%.06f".format(distance / AU_KM))
            else extra.add("Distance (km)" to "%.03f".format(distance))
        } else if (body.distance > 0.0) {
            extra.add("Distance (ly)" to "%.1f".format(body.distance))
        } else {
            extra.add("Distance" to "-")
        }

        val magnitude = element[HorizonsQuantity.VISUAL_MAGNITUDE]?.ifBlank { null }
        if (magnitude != null) extra.add("Magnitude" to magnitude)

        val illuminated = element[HorizonsQuantity.ILLUMINATED_FRACTION]?.ifBlank { null }
        if (illuminated != null) extra.add("Illuminated (%)" to illuminated)

        val elongation = element[HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE]?.split(",")?.first()
        if (elongation != null) extra.add("Elongation (deg)" to elongation)

        if (body is AxisSize) {
            if (body.majorAxis.value > 0.0 || body.minorAxis.value > 0.0) {
                extra.add("Size (arcmin)" to "%.2f x %.2f".format(body.minorAxis.arcmin, body.majorAxis.arcmin))
            }
        }

        view.updateInfo(bodyName, date, extra)

        if (target == MOON_TARGET) {
            element.updateMoonImage()
        }

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

        val request = Request.Builder()
            .url("https://sdo.gsfc.nasa.gov/assets/img/latest/latest_256_HMIIC.jpg")
            .build()

        okHttpClient.newCall(request)
            .await().use {
                val image = ImageIO.read(it.body.byteStream())
                view.updateSunImage(image)
            }
    }

    @Scheduled(cron = "0 */15 * * * *")
    private fun updateSunImageAtSchduledTime() = runBlockingIO {
        updateSunImage()
    }

    suspend fun HorizonsElement.updateMoonImage() = withIO {
        if (!view.showing) return@withIO

        // TODO: Compute angle and age offline.
        val sot = this@updateMoonImage[HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE]?.split(",") ?: return@withIO
        val angle = sot[0].toDouble()
        val leading = sot[1] == "/L"
        val phase = if (leading) 360.0 - angle else angle
        val age = 29.53058868 * (phase / 360.0)

        LOG.info("computed Moon phase. angle={}, age={}", phase, age)

        view.updateMoonImage(phase, age, Angle.ZERO)
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

    suspend fun searchStar(text: String, filter: SkyObjectService.Filter? = null) = withIO {
        withMain { computing.set(true) }
        starFilter = filter ?: starFilter
        view.populateStar(skyObjectService.searchStar(text, starFilter))
        withMain { computing.set(false) }
    }

    suspend fun searchDSO(text: String, filter: SkyObjectService.Filter? = null) = withIO {
        withMain { computing.set(true) }
        dsoFilter = filter ?: dsoFilter
        view.populateDSOs(skyObjectService.searchDSO(text, dsoFilter))
        withMain { computing.set(false) }
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

    fun applySettings(useCoordinatesFromMount: Boolean) {
        clearCache()

        if (useCoordinatesFromMount) {
            loadCoordinatesFromMount()
        } else {
            loadCoordinates(view.latitude, view.longitude, view.elevation)
        }

        preferenceService.bool("atlas.useCoordinatesFromMount", useCoordinatesFromMount)

        launch {
            computeSun(tabType == AtlasView.TabType.SUN)

            if (tabType != AtlasView.TabType.SUN) {
                computeTab()
            }
        }
    }

    fun savePreferences() {
        if (!view.initialized) return

        preferenceService.double("atlas.screen.x", max(0.0, view.x))
        preferenceService.double("atlas.screen.y", max(0.0, view.y))
    }

    fun loadPreferences() {
        preferenceService.double("atlas.screen.x")?.also { view.x = it }
        preferenceService.double("atlas.screen.y")?.also { view.y = it }

        val useCoordinatesFromMount = "atlas.useCoordinatesFromMount" !in preferenceService || preferenceService.bool("atlas.useCoordinatesFromMount")
        val longitude = preferenceService.double("atlas.longitude")?.rad ?: Angle.ZERO
        val latitude = preferenceService.double("atlas.latitude")?.rad ?: Angle.ZERO
        val elevation = preferenceService.double("atlas.elevation")?.au ?: Distance.ZERO

        if (useCoordinatesFromMount) {
            loadCoordinatesFromMount()
        } else {
            loadCoordinates(latitude, longitude, elevation)
        }

        view.loadCoordinates(useCoordinatesFromMount, latitude, longitude, elevation)
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

        private const val SUN_TARGET = "10"
        private const val MOON_TARGET = "301"

        private const val ASTRONOMICAL_TWILIGHT = -18.0
        private const val NAUTICAL_TWILIGHT = -12.0
        private const val CIVIL_TWILIGHT = -6.0

        @JvmStatic private val LOG = loggerFor<AtlasManager>()
        @JvmStatic private val RTS_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
        @JvmStatic private val MOON_BODY = VSOP87E.EARTH + ELPMPP02

        @JvmStatic private val PLANET_BODIES = mapOf<String, Body?>(
            SUN_TARGET to VSOP87E.SUN,
            "199" to VSOP87E.MERCURY,
            "299" to VSOP87E.VENUS,
            "499" to VSOP87E.MARS,
            "599" to VSOP87E.JUPITER,
            "699" to VSOP87E.SATURN,
            "799" to VSOP87E.URANUS,
            "899" to VSOP87E.NEPTUNE,
            "999" to null,
            "401" to null,
            "402" to null,
            "501" to null,
            "402" to null,
            "403" to null,
            "504" to null,
            "601" to null,
            "602" to null,
            "603" to null,
            "604" to null,
            "605" to null,
            "606" to null,
            "607" to null,
            "608" to null,
            "701" to GUST86.ARIEL,
            "702" to GUST86.UMBRIEL,
            "703" to GUST86.TITANIA,
            "704" to GUST86.OBERON,
            "705" to GUST86.MIRANDA,
            "801" to null,
            "901" to null,
        )

        @JvmStatic
        private fun MutableMap<Int, Body>.computeFixedStar(body: SkyObject): Body {
            return if (body.id !in this) {
                val star = FixedStar(
                    body.rightAscension, body.declination,
                    body.pmRA, body.pmDEC, body.parallax, body.radialVelocity,
                )

                LOG.info("created new fixed star for body. star={}, body={}", star, body)

                this[body.id] = star
                star
            } else {
                this[body.id]!!
            }
        }
    }
}
