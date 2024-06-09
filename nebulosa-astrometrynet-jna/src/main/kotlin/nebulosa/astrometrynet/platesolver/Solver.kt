package nebulosa.astrometrynet.platesolver

import com.sun.jna.Callback
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder

@FieldOrder(
    "indexes", "fieldXY", "pixelXScale", "predistort",
    "fieldXYOrig", "funitsLower", "funitsUpper", "logRatioToPrint",
    "logRatioToKeep", "logRatioToTune", "recordMatchCallback", "userData",
    "distanceFromQuadBonus", "verifyUniformize", "verifyDedup", "doTweak",
    "tweakAbOrder", "tweakAbpOrder", "verifyPix", "distractorRatio",
    "codetol", "quadSizeMin", "quadSizeMax", "startobj", "endobj", "parity",
    "useRaDec", "centerxyz", "r2", "logRatioBailThreshold",
    "logRatioStoplooking", "maxquads", "maxmatches", "setCrpix",
    "setCrpixCenter", "crpix", "moTemplate", "timerCallback",
    "quitNow", "numTries", "numMatches", "numScaleOk",
    "lastExaminedObject", "numCxDxSkipped", "numMeanxSkipped", "numRadecSkipped",
    "numAbscaleSkipped", "numVerified", "index", "minminAB2", "maxmaxAB2", "relIndexNoise2",
    "relFieldNoise2", "abscaleLow", "abscaleHigh", "fieldMinX",
    "fieldMaxX", "fieldMinY", "fieldMaxY", "fieldDiag",
    "cxdxMargin", "startTime", "timeUsed", "bestLogodds", "bestMatch", "bestIndex",
    "bestMatchSolves", "haveBestMatch", "vf",
)
sealed class Solver : Structure() {

    fun interface RecordMatchCallback : Callback {

        fun matchFound(matched: Matched.ByReference, userData: Pointer?): Byte
    }

    @JvmField var indexes: Pointer? = null
    @JvmField val fieldXY: Pointer? = null
    @JvmField var pixelXScale = 0.0
    @JvmField var predistort: Sip.ByReference? = null
    @JvmField var fieldXYOrig: Pointer? = null
    @JvmField var funitsLower = 0.0
    @JvmField var funitsUpper = 0.0
    @JvmField var logRatioToPrint = 0.0
    @JvmField var logRatioToKeep = 0.0
    @JvmField var logRatioToTune = 0.0
    @JvmField var recordMatchCallback: RecordMatchCallback? = null
    @JvmField var userData: Pointer? = null
    @JvmField var distanceFromQuadBonus: Byte = 0
    @JvmField var verifyUniformize: Byte = 0
    @JvmField var verifyDedup: Byte = 0
    @JvmField var doTweak: Byte = 0
    @JvmField var tweakAbOrder = 0
    @JvmField var tweakAbpOrder = 0

    // OPTIONAL
    @JvmField var verifyPix = 0.0
    @JvmField var distractorRatio = 0.0
    @JvmField var codetol = 0.0
    @JvmField var quadSizeMin = 0.1
    @JvmField var quadSizeMax = 1.0
    @JvmField var startobj = 0
    @JvmField var endobj = 0
    @JvmField var parity = 0
    @JvmField var useRaDec: Byte = 0
    @JvmField val centerxyz = DoubleArray(3)
    @JvmField var r2 = 0.0
    @JvmField var logRatioBailThreshold = 0.0
    @JvmField var logRatioStoplooking = 0.0
    @JvmField var maxquads = 0
    @JvmField var maxmatches = 0
    @JvmField var setCrpix: Byte = 0
    @JvmField var setCrpixCenter: Byte = 0
    @JvmField val crpix = DoubleArray(2)
    @JvmField var moTemplate: Pointer? = null
    @JvmField var timerCallback: Pointer? = null
    @JvmField var quitNow: Byte = 0

    // SOLVER OUTPUT
    @JvmField var numTries = 0
    @JvmField var numMatches = 0
    @JvmField var numScaleOk = 0
    @JvmField var lastExaminedObject = 0
    @JvmField var numCxDxSkipped = 0
    @JvmField var numMeanxSkipped = 0
    @JvmField var numRadecSkipped = 0
    @JvmField var numAbscaleSkipped = 0
    @JvmField var numVerified = 0
    @JvmField var index: Index.ByReference? = null
    @JvmField var minminAB2 = 0.0
    @JvmField var maxmaxAB2 = 0.0
    @JvmField var relIndexNoise2 = 0.0
    @JvmField var relFieldNoise2 = 0.0
    @JvmField var abscaleLow = 0.0
    @JvmField var abscaleHigh = 0.0
    @JvmField var fieldMinX = 0.0
    @JvmField var fieldMaxX = 0.0
    @JvmField var fieldMinY = 0.0
    @JvmField var fieldMaxY = 0.0
    @JvmField var fieldDiag = 0.0
    @JvmField var cxdxMargin = 0.0
    @JvmField var startTime = 0.0
    @JvmField var timeUsed = 0.0
    @JvmField var bestLogodds = 0.0
    @JvmField var bestMatch = Matched.ByValue()
    @JvmField var bestIndex: Index.ByReference? = null
    @JvmField var bestMatchSolves: Byte = 0
    @JvmField var haveBestMatch: Byte = 0
    @JvmField var vf: Pointer? = null

    class ByReference : Solver(), Structure.ByReference

    class ByValue : Solver(), Structure.ByValue

    override fun toString(): String {
        return "Solver(indexes=$indexes, fieldXY=$fieldXY, pixelXScale=$pixelXScale, " +
                "predistort=$predistort, fieldXYOrig=$fieldXYOrig, funitsLower=$funitsLower, " +
                "funitsUpper=$funitsUpper, logRatioToPrint=$logRatioToPrint, " +
                "logRatioToKeep=$logRatioToKeep, logRatioToTune=$logRatioToTune, " +
                "distanceFromQuadBonus=$distanceFromQuadBonus, verifyUniformize=$verifyUniformize, " +
                "verifyDedup=$verifyDedup, doTweak=$doTweak, tweakAbOrder=$tweakAbOrder, " +
                "tweakAbpOrder=$tweakAbpOrder, verifyPix=$verifyPix, distractorRatio=$distractorRatio, " +
                "codetol=$codetol, quadSizeMin=$quadSizeMin, quadSizeMax=$quadSizeMax, " +
                "startobj=$startobj, endobj=$endobj, parity=$parity, useRaDec=$useRaDec, " +
                "centerxyz=${centerxyz.contentToString()}, r2=$r2, " +
                "logRatioBailThreshold=$logRatioBailThreshold, logRatioStoplooking=$logRatioStoplooking, " +
                "maxquads=$maxquads, maxmatches=$maxmatches, setCrpix=$setCrpix, " +
                "setCrpixCenter=$setCrpixCenter, crpix=${crpix.contentToString()}, moTemplate=$moTemplate, " +
                "quitNow=$quitNow, numTries=$numTries, numMatches=$numMatches, " +
                "numScaleOk=$numScaleOk, lastExaminedObject=$lastExaminedObject, numCxDxSkipped=$numCxDxSkipped, " +
                "numMeanxSkipped=$numMeanxSkipped, numRadecSkipped=$numRadecSkipped, " +
                "numAbscaleSkipped=$numAbscaleSkipped, numVerified=$numVerified, index=$index, " +
                "minminAB2=$minminAB2, maxmaxAB2=$maxmaxAB2, relIndexNoise2=$relIndexNoise2, " +
                "relFieldNoise2=$relFieldNoise2, abscaleLow=$abscaleLow, abscaleHigh=$abscaleHigh, " +
                "fieldMinX=$fieldMinX, fieldMaxX=$fieldMaxX, fieldMinY=$fieldMinY, fieldMaxY=$fieldMaxY, " +
                "fieldDiag=$fieldDiag, cxdxMargin=$cxdxMargin, startTime=$startTime, timeUsed=$timeUsed, " +
                "bestLogodds=$bestLogodds, bestMatch=$bestMatch, bestIndex=$bestIndex, " +
                "bestMatchSolves=$bestMatchSolves, haveBestMatch=$haveBestMatch, vf=$vf)"
    }
}
