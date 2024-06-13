package nebulosa.astrometrynet.platesolver

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder("crval", "crpix", "cd", "imagew", "imageh", "sin")
sealed class Tan : Structure() {

    @JvmField val crval = DoubleArray(2)
    @JvmField val crpix = DoubleArray(2)
    @JvmField val cd = DoubleArray(4)
    @JvmField var imagew = 0.0
    @JvmField var imageh = 0.0
    @JvmField var sin = 0.toByte()

    class ByReference : Tan(), Structure.ByReference

    class ByValue : Tan(), Structure.ByValue

    override fun toString(): String {
        return "Tan(crval=${crval.contentToString()}, crpix=${crpix.contentToString()}, " +
                "cd=${cd.contentToString()}, imagew=$imagew, imageh=$imageh, sin=$sin)"
    }
}
