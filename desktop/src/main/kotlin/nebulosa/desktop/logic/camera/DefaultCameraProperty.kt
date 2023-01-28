package nebulosa.desktop.logic.camera

import javafx.beans.property.*
import javafx.collections.FXCollections
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.*
import nebulosa.indi.protocol.PropertyState

open class DefaultCameraProperty : AbstractDeviceProperty<Camera>(), CameraProperty {

    override val exposuringProperty = SimpleBooleanProperty(false)
    override val hasCoolerControlProperty = SimpleBooleanProperty(false)
    override val coolerProperty = SimpleBooleanProperty(false)
    override val hasDewHeaterProperty = SimpleBooleanProperty(false)
    override val dewHeaterProperty = SimpleBooleanProperty(false)
    override val frameFormatsProperty = SimpleListProperty(FXCollections.observableArrayList<String>())
    override val canAbortProperty = SimpleBooleanProperty(false)
    override val cfaOffsetXProperty = SimpleIntegerProperty(0)
    override val cfaOffsetYProperty = SimpleIntegerProperty(0)
    override val cfaTypeProperty = SimpleObjectProperty(CfaPattern.RGGB)
    override val exposureMinProperty = SimpleLongProperty(0L)
    override val exposureMaxProperty = SimpleLongProperty(0L)
    override val exposureStateProperty = SimpleObjectProperty(PropertyState.IDLE)
    override val hasCoolerProperty = SimpleBooleanProperty(false)
    override val canSetTemperatureProperty = SimpleBooleanProperty(false)
    override val temperatureProperty = SimpleDoubleProperty(0.0)
    override val canSubFrameProperty = SimpleBooleanProperty(false)
    override val xProperty = SimpleIntegerProperty(0)
    override val minXProperty = SimpleIntegerProperty(0)
    override val maxXProperty = SimpleIntegerProperty(0)
    override val yProperty = SimpleIntegerProperty(0)
    override val minYProperty = SimpleIntegerProperty(0)
    override val maxYProperty = SimpleIntegerProperty(0)
    override val widthProperty = SimpleIntegerProperty(0)
    override val minWidthProperty = SimpleIntegerProperty(0)
    override val maxWidthProperty = SimpleIntegerProperty(0)
    override val heightProperty = SimpleIntegerProperty(0)
    override val minHeightProperty = SimpleIntegerProperty(0)
    override val maxHeightProperty = SimpleIntegerProperty(0)
    override val canBinProperty = SimpleBooleanProperty(false)
    override val maxBinXProperty = SimpleIntegerProperty(1)
    override val maxBinYProperty = SimpleIntegerProperty(1)
    override val binXProperty = SimpleIntegerProperty(1)
    override val binYProperty = SimpleIntegerProperty(1)
    override val gainProperty = SimpleIntegerProperty(0)
    override val gainMinProperty = SimpleIntegerProperty(0)
    override val gainMaxProperty = SimpleIntegerProperty(0)
    override val offsetProperty = SimpleIntegerProperty(0)
    override val offsetMinProperty = SimpleIntegerProperty(0)
    override val offsetMaxProperty = SimpleIntegerProperty(0)

    override fun onChanged(prev: Camera?, device: Camera) {
        exposuringProperty.set(device.exposuring)
        hasCoolerControlProperty.set(device.hasCoolerControl)
        coolerProperty.set(device.cooler)
        hasDewHeaterProperty.set(device.hasDewHeater)
        dewHeaterProperty.set(device.dewHeater)
        frameFormatsProperty.setAll(device.frameFormats)
        canAbortProperty.set(device.canAbort)
        cfaOffsetXProperty.set(device.cfaOffsetX)
        cfaOffsetYProperty.set(device.cfaOffsetY)
        cfaTypeProperty.set(device.cfaType)
        exposureMinProperty.set(device.exposureMin)
        exposureMaxProperty.set(device.exposureMax)
        exposureStateProperty.set(device.exposureState)
        hasCoolerProperty.set(device.hasCooler)
        canSetTemperatureProperty.set(device.canSetTemperature)
        temperatureProperty.set(device.temperature)
        canSubFrameProperty.set(device.canSubFrame)
        xProperty.set(device.x)
        minXProperty.set(device.minX)
        maxXProperty.set(device.maxX)
        yProperty.set(device.y)
        minYProperty.set(device.minY)
        maxYProperty.set(device.maxY)
        widthProperty.set(device.width)
        minWidthProperty.set(device.minWidth)
        maxWidthProperty.set(device.maxWidth)
        heightProperty.set(device.height)
        minHeightProperty.set(device.minHeight)
        maxHeightProperty.set(device.maxHeight)
        canBinProperty.set(device.canBin)
        maxBinXProperty.set(device.maxBinX)
        maxBinYProperty.set(device.maxBinY)
        binXProperty.set(device.binX)
        binYProperty.set(device.binY)
        gainMinProperty.set(device.gainMin)
        gainMaxProperty.set(device.gainMax)
        gainProperty.set(device.gain)
        offsetMinProperty.set(device.offsetMin)
        offsetMaxProperty.set(device.offsetMax)
        offsetProperty.set(device.offset)
    }

    override fun onReset() {
        exposuringProperty.set(false)
        hasCoolerControlProperty.set(false)
        coolerProperty.set(false)
        hasDewHeaterProperty.set(false)
        dewHeaterProperty.set(false)
        frameFormatsProperty.clear()
        canAbortProperty.set(false)
        cfaOffsetXProperty.set(0)
        cfaOffsetYProperty.set(0)
        cfaTypeProperty.set(CfaPattern.RGGB)
        exposureMinProperty.set(0L)
        exposureMaxProperty.set(0L)
        exposureStateProperty.set(PropertyState.IDLE)
        hasCoolerProperty.set(false)
        canSetTemperatureProperty.set(false)
        temperatureProperty.set(0.0)
        canSubFrameProperty.set(false)
        xProperty.set(0)
        minXProperty.set(0)
        maxXProperty.set(0)
        yProperty.set(0)
        minYProperty.set(0)
        maxYProperty.set(0)
        widthProperty.set(0)
        minWidthProperty.set(0)
        maxWidthProperty.set(0)
        heightProperty.set(0)
        minHeightProperty.set(0)
        maxHeightProperty.set(0)
        canBinProperty.set(false)
        maxBinXProperty.set(1)
        maxBinYProperty.set(1)
        binXProperty.set(1)
        binYProperty.set(1)
        gainMinProperty.set(0)
        gainMaxProperty.set(0)
        gainProperty.set(0)
        offsetMinProperty.set(0)
        offsetMaxProperty.set(0)
        offsetProperty.set(0)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
        when (event) {
            is CameraExposuringChanged -> exposuringProperty.set(device.exposuring)
            is CameraCoolerControlChanged -> hasCoolerControlProperty.set(device.hasCoolerControl)
            is CameraCoolerChanged -> coolerProperty.set(device.cooler)
            is CameraHasDewHeaterChanged -> hasDewHeaterProperty.set(device.hasDewHeater)
            is CameraDewHeaterChanged -> dewHeaterProperty.set(device.dewHeater)
            is CameraFrameFormatsChanged -> frameFormatsProperty.setAll(device.frameFormats)
            is CameraCanAbortChanged -> canAbortProperty.set(device.canAbort)
            is CameraGainChanged -> gainProperty.set(device.gain)
            is CameraOffsetChanged -> offsetProperty.set(device.offset)
            is CameraExposureStateChanged -> exposureStateProperty.set(device.exposureState)
            is CameraHasCoolerChanged -> hasCoolerProperty.set(device.hasCooler)
            is CameraCanSetTemperatureChanged -> canSetTemperatureProperty.set(device.canSetTemperature)
            is CameraTemperatureChanged -> temperatureProperty.set(device.temperature)
            is CameraCanSubFrameChanged -> canSubFrameProperty.set(device.canSubFrame)
            is CameraCfaChanged -> {
                cfaOffsetXProperty.set(device.cfaOffsetX)
                cfaOffsetYProperty.set(device.cfaOffsetY)
                cfaTypeProperty.set(device.cfaType)
            }
            is CameraExposureMinMaxChanged -> {
                exposureMaxProperty.set(device.exposureMax)
                exposureMinProperty.set(device.exposureMin)
            }
            is CameraGainMinMaxChanged -> {
                gainMaxProperty.set(device.gainMax)
                gainMinProperty.set(device.gainMin)
            }
            is CameraOffsetMinMaxChanged -> {
                offsetMaxProperty.set(device.offsetMax)
                offsetMinProperty.set(device.offsetMin)
            }
            is CameraFrameChanged -> {
                maxXProperty.set(device.maxX)
                minXProperty.set(device.minX)
                maxYProperty.set(device.maxY)
                minYProperty.set(device.minY)
                maxWidthProperty.set(device.maxWidth)
                minWidthProperty.set(device.minWidth)
                maxHeightProperty.set(device.maxHeight)
                minHeightProperty.set(device.minHeight)
                xProperty.set(device.x)
                yProperty.set(device.y)
                widthProperty.set(device.width)
                heightProperty.set(device.height)
            }
            is CameraCanBinChanged -> {
                canBinProperty.set(device.canBin)
                maxBinXProperty.set(device.maxBinX)
                maxBinYProperty.set(device.maxBinY)
            }
            is CameraBinChanged -> {
                binXProperty.set(device.binX)
                binYProperty.set(device.binY)
            }
        }
    }
}
