package nebulosa.api.atlas.ephemeris

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.normalized
import nebulosa.math.toDegrees
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.VSOP87E
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.GeographicPosition
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import kotlin.system.measureTimeMillis

class BodyEphemerisProvider(private val executor: ExecutorService) : CachedEphemerisProvider<Body>() {

    private val timeBucket = HashMap<LocalDateTime, UTC>()
    private val cachedBodies = HashMap<GeographicPosition, Body>()

    override fun compute(
        target: Body, position: GeographicPosition,
        startTime: LocalDateTime, endTime: LocalDateTime,
    ): List<HorizonsElement> {
        val site = cachedBodies.getOrPut(position) { VSOP87E.EARTH + position }

        val intervalInMinutes = ChronoUnit.MINUTES.between(startTime, endTime).toInt() + 1
        val elementMap = LinkedHashMap<LocalDateTime, HorizonsElement>(intervalInMinutes)
        val elementQueue = LinkedList<HorizonsElement>()

        repeat(intervalInMinutes) {
            val time = startTime.plusMinutes(it.toLong())
            val element = HorizonsElement(time)
            elementMap[time] = element
            elementQueue.add(element)
        }

        val numberOfTasks = Runtime.getRuntime().availableProcessors()
        val tasks = ArrayList<CompletableFuture<*>>(numberOfTasks)

        repeat(numberOfTasks) {
            CompletableFuture.runAsync({
                while (true) {
                    val element = synchronized(elementQueue) {
                        elementQueue.removeFirstOrNull()
                    } ?: break

                    val utcTime = synchronized(timeBucket) {
                        val time = element.dateTime
                        timeBucket.getOrPut(time) { UTC(TimeYMDHMS(time)) }
                    }

                    val barycentric = site.at<Barycentric>(utcTime)
                    val astrometric = barycentric.observe(target)
                    val (az, alt) = astrometric.horizontal()
                    val (ra, dec) = astrometric.equatorialAtDate()
                    val (raJ2000, decJ2000) = astrometric.equatorial()

                    element[HorizonsQuantity.ASTROMETRIC_RA] = "${raJ2000.normalized.toDegrees}"
                    element[HorizonsQuantity.ASTROMETRIC_DEC] = "${decJ2000.toDegrees}"
                    element[HorizonsQuantity.APPARENT_RA] = "${ra.normalized.toDegrees}"
                    element[HorizonsQuantity.APPARENT_DEC] = "${dec.toDegrees}"
                    element[HorizonsQuantity.APPARENT_AZ] = "${az.normalized.toDegrees}"
                    element[HorizonsQuantity.APPARENT_ALT] = "${alt.toDegrees}"
                    val illuminatedFraction = if (target === VSOP87E.SUN) SUN_ILLUMINATED else astrometric.illuminated(VSOP87E.SUN) * 100.0
                    element[HorizonsQuantity.ILLUMINATED_FRACTION] = "$illuminatedFraction"
                    element[HorizonsQuantity.CONSTELLATION] = Constellation.find(astrometric).name
                    element[HorizonsQuantity.ONE_WAY_LIGHT_TIME] = (astrometric.lightTime * 1440.0).toString()
                    val (elongation, east) = if (target === VSOP87E.SUN) SUN_ELONGATION else barycentric.elongation(target, VSOP87E.SUN)
                    element[HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE] = "${elongation.toDegrees},/${if (east) 'L' else 'T'}"
                }
            }, executor).also(tasks::add)
        }

        val elapsedTime = measureTimeMillis { tasks.forEach { it.get() } }

        LOG.d("elapsed {} ms for computing body ephemeris", elapsedTime)

        return elementMap.values.toList()
    }

    companion object {

        private val LOG = loggerFor<BodyEphemerisProvider>()

        private const val SUN_ILLUMINATED = 100.0
        private val SUN_ELONGATION = 0.0 to true
    }
}
