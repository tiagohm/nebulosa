package nebulosa.api.cameras

import nebulosa.api.calibration.CalibrationFrameService.Companion.frameType
import nebulosa.common.concurrency.atomic.Incrementer
import nebulosa.fits.*
import nebulosa.image.format.ReadableHeader
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.math.AngleFormatter
import nebulosa.math.format
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

data class CameraCaptureNamingFormatter(
    @JvmField internal val camera: Camera,
    @JvmField internal val mount: Mount? = null,
    @JvmField internal val wheel: FilterWheel? = null,
    @JvmField internal val focuser: Focuser? = null,
    @JvmField internal val rotator: Rotator? = null,
    @JvmField internal val clock: Clock = Clock.systemDefaultZone(),
    @JvmField internal val incrementer: Incrementer = Incrementer(),
) {

    fun format(text: String, header: ReadableHeader): String {
        return REGEX.replace(text.trim()) { m ->
            val value = m.groups[1]!!.value
            val type = CameraCaptureNamingType.find(value)

            if (type == null) {
                value
            } else {
                val groupValues = m.groups.mapNotNull { it?.value?.ifBlank { null } }
                val arguments = if (groupValues.size > 2) groupValues.subList(2, groupValues.size) else emptyList()
                type.replaceWith(header, arguments)?.ifBlank { null } ?: ""
            }
        }.replace(ILLEGAL_CHARS_REGEX, "")
    }

    private fun CameraCaptureNamingType.replaceWith(header: ReadableHeader, args: List<String>): String? {
        return when (this) {
            CameraCaptureNamingType.TYPE -> header.frameType?.name?.ifBlank { null } ?: FrameType.LIGHT.name
            CameraCaptureNamingType.YEAR -> with(LocalDate.now(clock).year) {
                when (args.firstOrNull()) {
                    "2" -> "%02d".format(this - 2000)
                    else -> "$this"
                }
            }
            CameraCaptureNamingType.MONTH -> "%02d".format(LocalDate.now(clock).monthValue)
            CameraCaptureNamingType.DAY -> "%02d".format(LocalDate.now(clock).dayOfMonth)
            CameraCaptureNamingType.HOUR -> "%02d".format(LocalTime.now(clock).hour)
            CameraCaptureNamingType.MIN -> "%02d".format(LocalTime.now(clock).minute)
            CameraCaptureNamingType.SEC -> "%02d".format(LocalTime.now(clock).second)
            CameraCaptureNamingType.MS -> "%03d".format(LocalTime.now(clock).nano / 1000000)
            CameraCaptureNamingType.EXP -> with(header.exposureTime) {
                when (args.firstOrNull()) {
                    "s" -> "%ds".format(toSeconds())
                    "ms" -> "%dms".format(toMillis())
                    else -> (toNanos() / 1000).toString()
                }
            }
            CameraCaptureNamingType.FILTER -> header.filter?.ifBlank { null }
            CameraCaptureNamingType.GAIN -> "${header.gain.roundToInt()}"
            CameraCaptureNamingType.BIN -> "${header.binX}"
            CameraCaptureNamingType.TEMP -> "${header.temperature.roundToInt()}"
            CameraCaptureNamingType.RA -> header.rightAscension.takeIf { it.isFinite() }?.format(RA_FORMAT)
            CameraCaptureNamingType.DEC -> header.declination.takeIf { it.isFinite() }?.format(DEC_FORMAT)
            CameraCaptureNamingType.WIDTH -> "${header.width}"
            CameraCaptureNamingType.HEIGHT -> "${header.height}"
            CameraCaptureNamingType.CAMERA -> camera.name
            CameraCaptureNamingType.MOUNT -> mount?.name ?: camera.snoopedDevices.firstOrNull { it is Mount }?.name
            CameraCaptureNamingType.FOCUSER -> focuser?.name ?: camera.snoopedDevices.firstOrNull { it is Focuser }?.name
            CameraCaptureNamingType.WHEEL -> wheel?.name ?: camera.snoopedDevices.firstOrNull { it is FilterWheel }?.name
            CameraCaptureNamingType.ROTATOR -> rotator?.name ?: camera.snoopedDevices.firstOrNull { it is Rotator }?.name
            CameraCaptureNamingType.N -> with(incrementer.increment()) {
                val format = args.firstOrNull()?.toIntOrNull()?.absoluteValue ?: 4
                "%0${format}d".format(this)
            }
        }
    }

    companion object {

        @JvmStatic private val REGEX = Regex("\\[(\\w+)(?::(\\w+))*]")
        @JvmStatic private val ILLEGAL_CHARS_REGEX = Regex("[/\\\\:*?\"<>|]+")
        @JvmStatic private val RA_FORMAT = AngleFormatter.HMS.newBuilder().secondsDecimalPlaces(0).build()
        @JvmStatic private val DEC_FORMAT = AngleFormatter.SIGNED_DMS.newBuilder().secondsDecimalPlaces(0).separators("d", "m", "s").build()

        const val FLAT_FORMAT = "[camera]_[type]_[filter]_[width]_[height]_[bin]"
        const val DARK_FORMAT = "[camera]_[type]_[width]_[height]_[exp]_[bin]_[gain]"
        const val BIAS_FORMAT = "[camera]_[type]_[width]_[height]_[bin]_[gain]"
        const val LIGHT_FORMAT = "[camera]_[type]_[year:2][month][day][hour][min][sec][ms]_[filter]_[width]_[height]_[exp]_[bin]_[gain]"
    }
}
