package nebulosa.indi.device.mount

import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.Parkable
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.math.Angle
import nebulosa.math.Distance
import java.time.OffsetDateTime

interface Mount : GuideOutput, GPS, Parkable {

    override val type
        get() = DeviceType.MOUNT

    val slewing: Boolean

    val tracking: Boolean

    val canAbort: Boolean

    val canSync: Boolean

    val canGoTo: Boolean

    val canHome: Boolean

    val slewRates: List<SlewRate>

    val slewRate: SlewRate?

    val mountType: MountType

    val trackModes: List<TrackMode>

    val trackMode: TrackMode

    val pierSide: PierSide

    val guideRateWE: Double

    val guideRateNS: Double

    val rightAscension: Angle

    val declination: Angle

    fun tracking(enable: Boolean)

    fun sync(ra: Angle, dec: Angle)

    fun syncJ2000(ra: Angle, dec: Angle)

    fun slewTo(ra: Angle, dec: Angle)

    fun slewToJ2000(ra: Angle, dec: Angle)

    fun goTo(ra: Angle, dec: Angle)

    fun goToJ2000(ra: Angle, dec: Angle)

    fun home()

    fun abortMotion()

    fun trackMode(mode: TrackMode)

    fun slewRate(rate: SlewRate)

    fun moveNorth(enabled: Boolean)

    fun moveSouth(enabled: Boolean)

    fun moveWest(enabled: Boolean)

    fun moveEast(enabled: Boolean)

    fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance)

    fun dateTime(dateTime: OffsetDateTime)
}
