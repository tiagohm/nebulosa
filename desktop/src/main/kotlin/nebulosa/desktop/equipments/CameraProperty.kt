package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.protocol.PropertyState

class CameraProperty : DeviceProperty<Camera>() {

    @JvmField val isCapturing = SimpleBooleanProperty(false)
    @JvmField val hasCoolerControl = SimpleBooleanProperty(false)
    @JvmField val isCoolerOn = SimpleBooleanProperty(false)
    @JvmField val hasDewHeater = SimpleBooleanProperty(false)
    @JvmField val isDewHeaterOn = SimpleBooleanProperty(false)
    @JvmField val frameFormats = SimpleListProperty(FXCollections.observableArrayList<String>())
    @JvmField val canAbort = SimpleBooleanProperty(false)
    @JvmField val cfaOffsetX = SimpleIntegerProperty(0)
    @JvmField val cfaOffsetY = SimpleIntegerProperty(0)
    @JvmField val cfaType = SimpleObjectProperty(CfaPattern.RGGB)
    @JvmField val exposureMin = SimpleLongProperty(0L)
    @JvmField val exposureMax = SimpleLongProperty(0L)
    @JvmField val exposureState = SimpleObjectProperty(PropertyState.IDLE)
    @JvmField val exposure = SimpleLongProperty(0L)
    @JvmField val hasCooler = SimpleBooleanProperty(false)
    @JvmField val canSetTemperature = SimpleBooleanProperty(false)
    @JvmField val temperature = SimpleDoubleProperty(0.0)
    @JvmField val canSubFrame = SimpleBooleanProperty(false)
    @JvmField val x = SimpleIntegerProperty(0)
    @JvmField val minX = SimpleIntegerProperty(0)
    @JvmField val maxX = SimpleIntegerProperty(0)
    @JvmField val y = SimpleIntegerProperty(0)
    @JvmField val minY = SimpleIntegerProperty(0)
    @JvmField val maxY = SimpleIntegerProperty(0)
    @JvmField val width = SimpleIntegerProperty(0)
    @JvmField val minWidth = SimpleIntegerProperty(0)
    @JvmField val maxWidth = SimpleIntegerProperty(0)
    @JvmField val height = SimpleIntegerProperty(0)
    @JvmField val minHeight = SimpleIntegerProperty(0)
    @JvmField val maxHeight = SimpleIntegerProperty(0)
    @JvmField val canBin = SimpleBooleanProperty(false)
    @JvmField val maxBinX = SimpleIntegerProperty(1)
    @JvmField val maxBinY = SimpleIntegerProperty(1)
    @JvmField val binX = SimpleIntegerProperty(1)
    @JvmField val binY = SimpleIntegerProperty(1)
    @JvmField val gain = SimpleIntegerProperty(0)
    @JvmField val gainMin = SimpleIntegerProperty(0)
    @JvmField val gainMax = SimpleIntegerProperty(0)
    @JvmField val offset = SimpleIntegerProperty(0)
    @JvmField val offsetMin = SimpleIntegerProperty(0)
    @JvmField val offsetMax = SimpleIntegerProperty(0)

    override fun changed(value: Camera) {
        isCapturing.set(value.isCapturing)
        hasCoolerControl.set(value.hasCoolerControl)
        isCoolerOn.set(value.isCoolerOn)
        hasDewHeater.set(value.hasDewHeater)
        isDewHeaterOn.set(value.isDewHeaterOn)
        frameFormats.setAll(value.frameFormats)
        canAbort.set(value.canAbort)
        cfaOffsetX.set(value.cfaOffsetX)
        cfaOffsetY.set(value.cfaOffsetY)
        cfaType.set(value.cfaType)
        exposureMin.set(value.exposureMin)
        exposureMax.set(value.exposureMax)
        exposureState.set(value.exposureState)
        exposure.set(value.exposure)
        hasCooler.set(value.hasCooler)
        canSetTemperature.set(value.canSetTemperature)
        temperature.set(value.temperature)
        canSubFrame.set(value.canSubFrame)
        x.set(value.x)
        minX.set(value.minX)
        maxX.set(value.maxX)
        y.set(value.y)
        minY.set(value.minY)
        maxY.set(value.maxY)
        width.set(value.width)
        minWidth.set(value.minWidth)
        maxWidth.set(value.maxWidth)
        height.set(value.height)
        minHeight.set(value.minHeight)
        maxHeight.set(value.maxHeight)
        canBin.set(value.canBin)
        maxBinX.set(value.maxBinX)
        maxBinY.set(value.maxBinY)
        binX.set(value.binX)
        binY.set(value.binY)
        gainMin.set(value.gainMin)
        gainMax.set(value.gainMax)
        gain.set(value.gain)
        offsetMin.set(value.offsetMin)
        offsetMax.set(value.offsetMax)
        offset.set(value.offset)
    }

    override fun reset() {
        isCapturing.set(false)
        hasCoolerControl.set(false)
        isCoolerOn.set(false)
        hasDewHeater.set(false)
        isDewHeaterOn.set(false)
        frameFormats.clear()
        canAbort.set(false)
        cfaOffsetX.set(0)
        cfaOffsetY.set(0)
        cfaType.set(CfaPattern.RGGB)
        exposureMin.set(0L)
        exposureMax.set(0L)
        exposureState.set(PropertyState.IDLE)
        exposure.set(0L)
        hasCooler.set(false)
        canSetTemperature.set(false)
        temperature.set(0.0)
        canSubFrame.set(false)
        x.set(0)
        minX.set(0)
        maxX.set(0)
        y.set(0)
        minY.set(0)
        maxY.set(0)
        width.set(0)
        minWidth.set(0)
        maxWidth.set(0)
        height.set(0)
        minHeight.set(0)
        maxHeight.set(0)
        canBin.set(false)
        maxBinX.set(1)
        maxBinY.set(1)
        binX.set(1)
        binY.set(1)
        gainMin.set(0)
        gainMax.set(0)
        gain.set(0)
        offsetMin.set(0)
        offsetMax.set(0)
        offset.set(0)
    }

    override fun accept(event: DeviceEvent<*>) {
        when (event) {
            is CameraCapturingChanged -> Platform.runLater { isCapturing.set(value.isCapturing) }
            is CameraCoolerControlChanged -> Platform.runLater { hasCoolerControl.set(value.hasCoolerControl) }
            is CameraCoolerChanged -> Platform.runLater { isCoolerOn.set(value.isCoolerOn) }
            is CameraHasDewHeaterChanged -> Platform.runLater { hasDewHeater.set(value.hasDewHeater) }
            is CameraDewHeaterChanged -> Platform.runLater { isDewHeaterOn.set(value.isDewHeaterOn) }
            is CameraFrameFormatsChanged -> Platform.runLater { frameFormats.setAll(value.frameFormats) }
            is CameraCanAbortChanged -> Platform.runLater { canAbort.set(value.canAbort) }
            is CameraCfaChanged -> Platform.runLater {
                cfaOffsetX.set(value.cfaOffsetX)
                cfaOffsetY.set(value.cfaOffsetY)
                cfaType.set(value.cfaType)
            }
            is CameraExposureMinMaxChanged -> Platform.runLater {
                exposureMin.set(value.exposureMin)
                exposureMax.set(value.exposureMax)
            }
            is CameraGainChanged -> Platform.runLater { gain.set(value.gain) }
            is CameraGainMinMaxChanged -> Platform.runLater {
                gainMin.set(value.gainMin)
                gainMax.set(value.gainMax)
            }
            is CameraOffsetChanged -> Platform.runLater { offset.set(value.offset) }
            is CameraOffsetMinMaxChanged -> Platform.runLater {
                offsetMin.set(value.offsetMin)
                offsetMax.set(value.offsetMax)
            }
            is CameraExposureStateChanged -> Platform.runLater { exposureState.set(value.exposureState) }
            is CameraHasCoolerChanged -> Platform.runLater { hasCooler.set(value.hasCooler) }
            is CameraCanSetTemperatureChanged -> Platform.runLater { canSetTemperature.set(value.canSetTemperature) }
            is CameraTemperatureChanged -> Platform.runLater { temperature.set(value.temperature) }
            is CameraCanSubFrameChanged -> Platform.runLater { canSubFrame.set(value.canSubFrame) }
            is CameraFrameChanged -> Platform.runLater {
                minX.set(value.minX)
                maxX.set(value.maxX)
                minY.set(value.minY)
                maxY.set(value.maxY)
                minWidth.set(value.minWidth)
                maxWidth.set(value.maxWidth)
                minHeight.set(value.minHeight)
                maxHeight.set(value.maxHeight)
                x.set(value.x)
                y.set(value.y)
                width.set(value.width)
                height.set(value.height)
            }
            is CameraCanBinChanged -> Platform.runLater {
                canBin.set(value.canBin)
                maxBinX.set(value.maxBinX)
                maxBinY.set(value.maxBinY)
            }
            is CameraBinChanged -> Platform.runLater {
                binX.set(value.binX)
                binY.set(value.binY)
            }
        }
    }
}
