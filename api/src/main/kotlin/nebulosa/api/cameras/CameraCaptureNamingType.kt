package nebulosa.api.cameras

enum class CameraCaptureNamingType(private vararg val alias: String) {
    TYPE,
    YEAR,
    MONTH,
    DAY,
    HOUR,
    MIN("minute"),
    SEC("second"),
    MS,
    EXP("exposure"),
    FILTER,
    GAIN,
    BIN,
    WIDTH,
    HEIGHT,
    TEMP("temperature"),
    RA,
    DEC,
    CAMERA,
    MOUNT,
    FOCUSER,
    WHEEL,
    ROTATOR,
    N;

    companion object {

        @JvmStatic
        fun find(text: String): CameraCaptureNamingType? {
            return entries.firstOrNull { it.name.equals(text, true) }
                ?: entries.firstOrNull { e -> e.alias.isNotEmpty() && e.alias.any { it.equals(text, true) } }
        }
    }
}
