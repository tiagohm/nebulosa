package nebulosa.astrometrynet.platesolver

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import nebulosa.astrometrynet.platesolver.LibAstrometryNet.Companion.DQMAX

@FieldOrder(
    "quadno", "star", "field", "ids", "codeError", "quadpix", "quadpixOrig", "quadxyz",
    "dimquads", "center", "radius", "radiusDeg", "wcsIsValid", "wcstan",
    "scale", "quadNPeers", "nmatch", "ndistractor", "nconflict", "nfield",
    "nindex", "nbest", "logodds", "worstLogodds", "nagree", "fieldNum",
    "fieldFile", "indexId", "healpix", "hpnside", "fieldname", "parity",
    "quadsTried", "quadsMatched", "quadsScaleOk", "objsTried", "nverified",
    "timeused", "sip", "refradec", "fieldxy", "fieldxyOrig", "tagalong",
    "fieldTagalong", "indexJitter", "index", "theta", "matchodds", "testperm",
    "refxyz", "refxy", "refstarId",
)
sealed class Matched : Structure() {

    @JvmField var quadno = 0
    @JvmField val star = IntArray(DQMAX)
    @JvmField val field = IntArray(DQMAX)
    @JvmField val ids = LongArray(DQMAX)
    @JvmField var codeError = 0f
    @JvmField val quadpix = DoubleArray(2 * DQMAX)
    @JvmField val quadpixOrig = DoubleArray(2 * DQMAX)
    @JvmField val quadxyz = DoubleArray(3 * DQMAX)
    @JvmField var dimquads = 0.toByte()
    @JvmField val center = DoubleArray(3)
    @JvmField var radius = 0.0
    @JvmField var radiusDeg = 0.0
    @JvmField var wcsIsValid = 0.toByte()
    @JvmField var wcstan = Tan.ByValue()
    @JvmField var scale = 0.0
    @JvmField var quadNPeers = 0.toShort()
    @JvmField var nmatch = 0
    @JvmField var ndistractor = 0
    @JvmField var nconflict = 0
    @JvmField var nfield = 0
    @JvmField var nindex = 0
    @JvmField var nbest = 0
    @JvmField var logodds = 0f
    @JvmField var worstLogodds = 0f
    @JvmField var nagree = 0.toShort()
    @JvmField var fieldNum = 0
    @JvmField var fieldFile = 0
    @JvmField var indexId = 0.toShort()
    @JvmField var healpix = 0.toShort()
    @JvmField var hpnside = 0.toShort()
    @JvmField val fieldname = ByteArray(32)
    @JvmField var parity = 0.toByte()
    @JvmField var quadsTried = 0
    @JvmField var quadsMatched = 0
    @JvmField var quadsScaleOk = 0
    @JvmField var objsTried = 0
    @JvmField var nverified = 0
    @JvmField var timeused = 0f
    @JvmField var sip: Sip.ByReference? = null
    @JvmField var refradec: DoubleByReference? = null
    @JvmField var fieldxy: DoubleByReference? = null
    @JvmField var fieldxyOrig: DoubleByReference? = null
    @JvmField var tagalong: Pointer? = null
    @JvmField var fieldTagalong: Pointer? = null
    @JvmField var indexJitter = 0.0
    @JvmField var index: Index.ByReference? = null
    @JvmField var theta: IntByReference? = null
    @JvmField var matchodds: DoubleByReference? = null
    @JvmField var testperm: IntByReference? = null
    @JvmField var refxyz: DoubleByReference? = null
    @JvmField var refxy: DoubleByReference? = null
    @JvmField var refstarId: IntByReference? = null

    class ByReference : Matched(), Structure.ByReference

    class ByValue : Matched(), Structure.ByValue

    override fun toString(): String {
        return "Matched(quadno=$quadno, star=${star.contentToString()}, " +
                "field=${field.contentToString()}, ids=${ids.contentToString()}, " +
                "codeError=$codeError, quadpix=${quadpix.contentToString()}, " +
                "quadpixOrig=${quadpixOrig.contentToString()}, quadxyz=${quadxyz.contentToString()}, " +
                "dimquads=$dimquads, center=${center.contentToString()}, radius=$radius, " +
                "radiusDeg=$radiusDeg, wcsIsValid=$wcsIsValid, wcstan=$wcstan, scale=$scale, " +
                "quadNPeers=$quadNPeers, nmatch=$nmatch, ndistractor=$ndistractor, " +
                "nconflict=$nconflict, nfield=$nfield, nindex=$nindex, nbest=$nbest, " +
                "logodds=$logodds, worstLogodds=$worstLogodds, nagree=$nagree, fieldNum=$fieldNum, " +
                "fieldFile=$fieldFile, indexId=$indexId, healpix=$healpix, hpnside=$hpnside, " +
                "fieldname=${fieldname.contentToString()}, parity=$parity, quadsTried=$quadsTried, " +
                "quadsMatched=$quadsMatched, quadsScaleOk=$quadsScaleOk, objsTried=$objsTried, " +
                "nverified=$nverified, timeused=$timeused, sip=$sip, refradec=$refradec, " +
                "fieldxy=$fieldxy, fieldxyOrig=$fieldxyOrig, tagalong=$tagalong, " +
                "fieldTagalong=$fieldTagalong, indexJitter=$indexJitter, index=$index, theta=$theta, " +
                "matchodds=$matchodds, testperm=$testperm, refxyz=$refxyz, refxy=$refxy, " +
                "refstarId=$refstarId)"
    }
}
