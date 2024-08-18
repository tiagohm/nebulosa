package nebulosa.indi.device.camera

import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.HeaderCard
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.protocol.PropertyState
import java.time.Duration

interface Camera : GuideOutput, Thermometer {

    override val type
        get() = DeviceType.CAMERA

    val exposuring: Boolean

    val hasCoolerControl: Boolean

    val coolerPower: Double

    val cooler: Boolean

    val hasDewHeater: Boolean

    val dewHeater: Boolean

    val frameFormats: List<String>

    val canAbort: Boolean

    val cfaOffsetX: Int

    val cfaOffsetY: Int

    val cfaType: CfaPattern

    val exposureMin: Duration

    val exposureMax: Duration

    val exposureState: PropertyState

    val exposureTime: Duration

    val hasCooler: Boolean

    val canSetTemperature: Boolean

    val canSubFrame: Boolean

    val x: Int

    val minX: Int

    val maxX: Int

    val y: Int

    val minY: Int

    val maxY: Int

    val width: Int

    val minWidth: Int

    val maxWidth: Int

    val height: Int

    val minHeight: Int

    val maxHeight: Int

    val canBin: Boolean

    val maxBinX: Int

    val maxBinY: Int

    val binX: Int

    val binY: Int

    val gain: Int

    val gainMin: Int

    val gainMax: Int

    val offset: Int

    val offsetMin: Int

    val offsetMax: Int

    val guideHead: GuideHead?

    val pixelSizeX: Double

    val pixelSizeY: Double

    fun cooler(enabled: Boolean)

    fun dewHeater(enabled: Boolean)

    fun temperature(value: Double)

    fun frameFormat(format: String?)

    fun frameType(type: FrameType)

    fun frame(x: Int, y: Int, width: Int, height: Int)

    fun bin(x: Int, y: Int)

    fun gain(value: Int)

    fun offset(value: Int)

    fun startCapture(exposureTime: Duration)

    fun abortCapture()

    fun fitsKeywords(vararg cards: HeaderCard)

    companion object {

        const val NANO_TO_SECONDS = 1_000_000_000.0
    }
}
