package nebulosa.api.stacker

import nebulosa.fits.filter
import nebulosa.image.format.ReadableHeader

enum class StackerGroupType {
    NONE,
    LUMINANCE,
    RED,
    GREEN,
    BLUE,
    MONO,
    RGB;

    inline val isRGB
        get() = this == RED || this == GREEN || this == BLUE

    inline val isLRGB
        get() = this == LUMINANCE || isRGB

    companion object {

        @JvmStatic
        fun from(header: ReadableHeader) = header.filter?.let {
            if (it.contains("RED", true) || it.equals("R", true)) RED
            else if (it.contains("GREEN", true) || it.equals("G", true)) GREEN
            else if (it.contains("BLUE", true) || it.equals("B", true)) BLUE
            else if (it.contains("LUMINANCE", true) || it.equals("L", true)) LUMINANCE
            else MONO
        } ?: MONO
    }
}
