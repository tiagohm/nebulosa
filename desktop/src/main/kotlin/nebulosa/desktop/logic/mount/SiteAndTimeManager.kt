package nebulosa.desktop.logic.mount

import nebulosa.desktop.view.mount.SiteAndTimeView
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SiteAndTimeManager(private val view: SiteAndTimeView) {

    fun openINDIPanelControl() {
        view.openINDIPanelControl(view.gps ?: return)
    }

    fun useCoordinateFromGPS() {
        val gps = view.gps ?: return
        view.updateSite(gps.longitude, gps.latitude, gps.elevation)
        view.mount.snoop(listOf(gps))
    }

    fun syncDateAndTime() {
        val locaDateTime = LocalDateTime.now(ZoneOffset.UTC)
        val offset = ZoneOffset.systemDefault().rules.getOffset(locaDateTime)
        val dateTime = OffsetDateTime.of(locaDateTime, offset)
        view.mount.time(dateTime)
        view.updateDateAndTime(dateTime)
    }

    fun applySite() {
        view.mount.coordinates(view.longitude, view.latitude, view.elevation)
    }

    fun applyDateAndTime() {
        val locaDateTime = LocalDateTime.of(view.date, view.time)
        val offset = ZoneOffset.ofTotalSeconds((view.offset * 60.0).toInt())
        val dateTime = OffsetDateTime.of(locaDateTime, offset)
        view.mount.time(dateTime)
    }
}
