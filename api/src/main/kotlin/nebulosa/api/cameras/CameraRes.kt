package nebulosa.api.cameras

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.FrameFormat
import nebulosa.indi.devices.cameras.CfaPattern
import nebulosa.indi.protocol.PropertyState

data class CameraRes(
    val isConnected: Boolean,
    val name: String,
    val hasCoolerControl: Boolean,
    val isCoolerOn: Boolean,
    val frameFormats: List<FrameFormat>,
    val canAbort: Boolean,
    val cfaOffsetX: Int,
    val cfaOffsetY: Int,
    val cfaType: CfaPattern,
    val exposureMin: Long,
    val exposureMax: Long,
    val exposureState: PropertyState,
    val hasCooler: Boolean,
    val canSetTemperature: Boolean,
    val temperature: Double,
    val canSubframe: Boolean,
    val x: Int,
    val minX: Int,
    val maxX: Int,
    val y: Int,
    val minY: Int,
    val maxY: Int,
    val width: Int,
    val minWidth: Int,
    val maxWidth: Int,
    val height: Int,
    val minHeight: Int,
    val maxHeight: Int,
    val canBin: Boolean,
    val maxBinX: Int,
    val maxBinY: Int,
    val binX: Int,
    val binY: Int,
) {

    companion object {

        @JvmStatic
        fun from(camera: Camera) = CameraRes(
            camera.isConnected,
            camera.name,
            camera.hasCoolerControl,
            camera.isCoolerOn,
            camera.frameFormats,
            camera.canAbort,
            camera.cfaOffsetX,
            camera.cfaOffsetY,
            camera.cfaType,
            camera.exposureMin,
            camera.exposureMax,
            camera.exposureState,
            camera.hasCooler,
            camera.canSetTemperature,
            camera.temperature,
            camera.canSubframe,
            camera.x,
            camera.minX,
            camera.maxX,
            camera.y,
            camera.minY,
            camera.maxY,
            camera.width,
            camera.minWidth,
            camera.maxWidth,
            camera.height,
            camera.minHeight,
            camera.maxHeight,
            camera.canBin,
            camera.maxBinX,
            camera.maxBinY,
            camera.binX,
            camera.binY,
        )
    }
}
