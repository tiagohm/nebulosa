package nebulosa.astrometrynet.plate.solving

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder(
    "codekd", "quads", "starkd", "fits", "indexFn", "indexName", "indexId", "healpix",
    "hpnside", "indexJitter", "cutnside", "cutnsweep", "cutdedup", "cutband", "cutmargin",
    "circle", "cxLessThanDx", "meanXLessThenHalf", "indexScaleUpper", "indexScaleLower",
    "dimquads", "nstars", "nquads"
)
sealed class Index : Structure() {

    @JvmField var codekd: Pointer? = Pointer.NULL
    @JvmField var quads: Pointer? = Pointer.NULL
    @JvmField var starkd: Pointer? = Pointer.NULL
    @JvmField var fits: Pointer? = Pointer.NULL
    @JvmField var indexFn: String? = null
    @JvmField var indexName: String? = null
    @JvmField var indexId = 0
    @JvmField var healpix = 0
    @JvmField var hpnside = 0
    @JvmField var indexJitter = 0.0
    @JvmField var cutnside = 0
    @JvmField var cutnsweep = 0
    @JvmField var cutdedup = 0.0
    @JvmField var cutband: String? = null
    @JvmField var cutmargin = 0
    @JvmField var circle = 0.toByte()
    @JvmField var cxLessThanDx = 0.toByte()
    @JvmField var meanXLessThenHalf = 0.toByte()
    @JvmField var indexScaleUpper = 0.0
    @JvmField var indexScaleLower = 0.0
    @JvmField var dimquads = 0
    @JvmField var nstars = 0
    @JvmField var nquads = 0

    class ByReference : Index(), Structure.ByReference

    class ByValue : Index(), Structure.ByValue

    override fun toString(): String {
        return "Index(codekd=$codekd, quads=$quads, starkd=$starkd, fits=$fits, " +
                "indexFn=$indexFn, indexName=$indexName, indexId=$indexId, healpix=$healpix, " +
                "hpnside=$hpnside, indexJitter=$indexJitter, cutnside=$cutnside, " +
                "cutnsweep=$cutnsweep, cutdedup=$cutdedup, cutband=$cutband, " +
                "cutmargin=$cutmargin, circle=$circle, cxLessThanDx=$cxLessThanDx, " +
                "meanXLessThenHalf=$meanXLessThenHalf, indexScaleUpper=$indexScaleUpper, " +
                "indexScaleLower=$indexScaleLower, dimquads=$dimquads, nstars=$nstars, " +
                "nquads=$nquads)"
    }
}
