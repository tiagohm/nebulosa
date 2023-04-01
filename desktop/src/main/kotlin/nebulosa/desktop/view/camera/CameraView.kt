package nebulosa.desktop.view.camera

import nebulosa.desktop.view.View
import nebulosa.indi.device.camera.FrameType
import java.util.concurrent.TimeUnit

interface CameraView : View {

    val frameMaxX: Int

    val frameMinX: Int

    val frameX: Int

    val frameMaxY: Int

    val frameMinY: Int

    val frameY: Int

    val frameMaxWidth: Int

    val frameMinWidth: Int

    val frameMaxHeight: Int

    val frameMinHeight: Int

    val frameWidth: Int

    val frameHeight: Int

    fun updateFrameMinMax(
        minX: Int, maxX: Int, minY: Int, maxY: Int,
        minWidth: Int, maxWidth: Int, minHeight: Int, maxHeight: Int
    )

    fun updateFrame(x: Int, y: Int, width: Int, height: Int)

    val maxBinX: Int

    val binX: Int

    val maxBinY: Int

    val binY: Int

    fun updateMaxBin(binX: Int, binY: Int)

    fun updateBin(x: Int, y: Int)

    val gainMax: Int

    val gainMin: Int

    val gain: Int

    fun updateGainMinMax(gainMin: Int, gainMax: Int)

    val offsetMax: Int

    val offsetMin: Int

    val offset: Int

    fun updateOffsetMinMax(offsetMin: Int, offsetMax: Int)

    fun updateGainAndOffset(gain: Int, offset: Int)

    val exposure: Long

    val exposureMax: Long

    val exposureMin: Long

    fun updateExposureMinMax(exposureMin: Long, exposureMax: Long)

    val exposureUnit: TimeUnit

    fun updateExposure(exposure: Long, unit: TimeUnit)

    var isAutoSaveAllExposures: Boolean

    var isAutoSubFolder: Boolean

    var autoSubFolderMode: AutoSubFolderMode

    var temperatureSetpoint: Double

    var imageSavePath: String

    var exposureMode: ExposureMode

    var exposureCount: Int

    val exposureInMicros
        get() = exposureUnit.toMicros(exposure)

    var exposureDelay: Long

    var isSubFrame: Boolean

    var frameFormat: String?

    var frameType: FrameType

    fun updateStatus(text: String)
}
