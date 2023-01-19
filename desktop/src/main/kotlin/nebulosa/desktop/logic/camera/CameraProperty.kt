package nebulosa.desktop.logic.camera

import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.cameras.*
import nebulosa.indi.protocol.PropertyState

open class CameraProperty : DeviceProperty<Camera>() {

    @JvmField val isExposuring = SimpleBooleanProperty(false)
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

    override fun changed(prev: Camera?, new: Camera) {
        isExposuring.set(new.isExposuring)
        hasCoolerControl.set(new.hasCoolerControl)
        isCoolerOn.set(new.isCoolerOn)
        hasDewHeater.set(new.hasDewHeater)
        isDewHeaterOn.set(new.isDewHeaterOn)
        frameFormats.setAll(new.frameFormats)
        canAbort.set(new.canAbort)
        cfaOffsetX.set(new.cfaOffsetX)
        cfaOffsetY.set(new.cfaOffsetY)
        cfaType.set(new.cfaType)
        exposureMin.set(new.exposureMin)
        exposureMax.set(new.exposureMax)
        exposureState.set(new.exposureState)
        hasCooler.set(new.hasCooler)
        canSetTemperature.set(new.canSetTemperature)
        temperature.set(new.temperature)
        canSubFrame.set(new.canSubFrame)
        x.set(new.x)
        minX.set(new.minX)
        maxX.set(new.maxX)
        y.set(new.y)
        minY.set(new.minY)
        maxY.set(new.maxY)
        width.set(new.width)
        minWidth.set(new.minWidth)
        maxWidth.set(new.maxWidth)
        height.set(new.height)
        minHeight.set(new.minHeight)
        maxHeight.set(new.maxHeight)
        canBin.set(new.canBin)
        maxBinX.set(new.maxBinX)
        maxBinY.set(new.maxBinY)
        binX.set(new.binX)
        binY.set(new.binY)
        gainMin.set(new.gainMin)
        gainMax.set(new.gainMax)
        gain.set(new.gain)
        offsetMin.set(new.offsetMin)
        offsetMax.set(new.offsetMax)
        offset.set(new.offset)
    }

    override fun reset() {
        isExposuring.set(false)
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

    override fun accept(event: DeviceEvent<Camera>) {
        val device = event.device!!

        when (event) {
            is CameraExposuringChanged -> Platform.runLater { isExposuring.set(device.isExposuring) }
            is CameraCoolerControlChanged -> Platform.runLater { hasCoolerControl.set(device.hasCoolerControl) }
            is CameraCoolerChanged -> Platform.runLater { isCoolerOn.set(device.isCoolerOn) }
            is CameraHasDewHeaterChanged -> Platform.runLater { hasDewHeater.set(device.hasDewHeater) }
            is CameraDewHeaterChanged -> Platform.runLater { isDewHeaterOn.set(device.isDewHeaterOn) }
            is CameraFrameFormatsChanged -> Platform.runLater { frameFormats.setAll(device.frameFormats) }
            is CameraCanAbortChanged -> Platform.runLater { canAbort.set(device.canAbort) }
            is CameraCfaChanged -> Platform.runLater {
                cfaOffsetX.set(device.cfaOffsetX)
                cfaOffsetY.set(device.cfaOffsetY)
                cfaType.set(device.cfaType)
            }
            is CameraExposureMinMaxChanged -> Platform.runLater {
                exposureMin.set(device.exposureMin)
                exposureMax.set(device.exposureMax)
            }
            is CameraGainChanged -> Platform.runLater { gain.set(device.gain) }
            is CameraGainMinMaxChanged -> Platform.runLater {
                gainMin.set(device.gainMin)
                gainMax.set(device.gainMax)
            }
            is CameraOffsetChanged -> Platform.runLater { offset.set(device.offset) }
            is CameraOffsetMinMaxChanged -> Platform.runLater {
                offsetMin.set(device.offsetMin)
                offsetMax.set(device.offsetMax)
            }
            is CameraExposureStateChanged -> Platform.runLater { exposureState.set(device.exposureState) }
            is CameraHasCoolerChanged -> Platform.runLater { hasCooler.set(device.hasCooler) }
            is CameraCanSetTemperatureChanged -> Platform.runLater { canSetTemperature.set(device.canSetTemperature) }
            is CameraTemperatureChanged -> Platform.runLater { temperature.set(device.temperature) }
            is CameraCanSubFrameChanged -> Platform.runLater { canSubFrame.set(device.canSubFrame) }
            is CameraFrameChanged -> Platform.runLater {
                minX.set(device.minX)
                maxX.set(device.maxX)
                minY.set(device.minY)
                maxY.set(device.maxY)
                minWidth.set(device.minWidth)
                maxWidth.set(device.maxWidth)
                minHeight.set(device.minHeight)
                maxHeight.set(device.maxHeight)
                x.set(device.x)
                y.set(device.y)
                width.set(device.width)
                height.set(device.height)
            }
            is CameraCanBinChanged -> Platform.runLater {
                canBin.set(device.canBin)
                maxBinX.set(device.maxBinX)
                maxBinY.set(device.maxBinY)
            }
            is CameraBinChanged -> Platform.runLater {
                binX.set(device.binX)
                binY.set(device.binY)
            }
        }
    }
}
