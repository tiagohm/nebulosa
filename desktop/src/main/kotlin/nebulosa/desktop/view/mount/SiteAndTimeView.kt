package nebulosa.desktop.view.mount

import nebulosa.desktop.view.View
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import nebulosa.math.Distance
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

interface SiteAndTimeView : View {

    val mount: Mount

    val longitude: Angle

    val latitude: Angle

    val elevation: Distance

    val gps: GPS?

    val date: LocalDate

    val time: LocalTime

    val offset: Double

    fun updateSite(longitude: Angle, latitude: Angle, elevation: Distance)

    fun updateDateAndTime(dateTime: OffsetDateTime)

    fun openINDIPanelControl(gps: GPS)
}
