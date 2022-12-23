package nebulosa.server.equipments

import nebulosa.grpc.CameraEquipment
import nebulosa.indi.devices.cameras.Camera

fun Camera.toCameraEquipment() = CameraEquipment.newBuilder()
    .setName(name)
    .setConnected(isConnected)
    .setHasCoolerControl(hasCoolerControl)
    .setIsCoolerOn(isCoolerOn)
    .addAllFrameFormats(frameFormats.map { it.name })
    .setCanAbort(canAbort)
    .setCfaOffsetX(cfaOffsetX)
    .setCfaOffsetY(cfaOffsetY)
    .setCfaType(cfaType.name)
    .setExposureMin(exposureMin)
    .setExposureMax(exposureMax)
    .setExposureState(exposureState.name)
    .setHasCooler(hasCooler)
    .setCanSetTemperature(canSetTemperature)
    .setTemperature(temperature)
    .setCanSubframe(canSubframe)
    .setMinX(minX)
    .setMaxX(maxX)
    .setMinY(minY)
    .setMaxY(maxY)
    .setMinWidth(minWidth)
    .setMaxWidth(maxWidth)
    .setMinHeight(minHeight)
    .setMaxHeight(maxHeight)
    .setCanBin(canBin)
    .setMaxBinX(maxBinX)
    .setMaxBinY(maxBinY)
    .build()!!
