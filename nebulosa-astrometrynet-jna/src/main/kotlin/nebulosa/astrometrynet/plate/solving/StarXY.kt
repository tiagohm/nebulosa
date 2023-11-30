package nebulosa.astrometrynet.plate.solving

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder("x", "y", "flux", "background", "n", "xlo", "xhi", "ylo", "yhi")
sealed class StarXY : Structure() {

    @JvmField var x: Pointer? = null
    @JvmField var y: Pointer? = null
    @JvmField var flux: Pointer? = null
    @JvmField var background: Pointer? = null
    @JvmField var n: Int = 0
    @JvmField var xlo = 0.0
    @JvmField var xhi = 0.0
    @JvmField var ylo = 0.0
    @JvmField var yhi = 0.0

    class ByReference : StarXY(), Structure.ByReference

    class ByValue : StarXY(), Structure.ByValue

    override fun toString(): String {
        return "StarXY(x=$x, y=$y, flux=$flux, background=$background, n=$n, " +
                "xlo=$xlo, xhi=$xhi, ylo=$ylo, yhi=$yhi)"
    }
}
