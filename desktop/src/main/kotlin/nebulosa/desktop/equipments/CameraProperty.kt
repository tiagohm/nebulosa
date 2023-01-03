package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.application.Platform
import javafx.beans.property.*
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import nebulosa.desktop.core.EventBus
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.*
import nebulosa.indi.protocol.PropertyState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CameraProperty : SimpleObjectProperty<Camera>(), ChangeListener<Camera>, Consumer<Any>, KoinComponent {

    private val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)
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

    init {
        addListener(this)

        eventBus.subscribe(this)
    }

    override fun changed(
        observable: ObservableValue<out Camera>,
        oldValue: Camera?, newValue: Camera?,
    ) {
        if (newValue == null) {
            reset()
        } else {
            isConnected.value = newValue.isConnected
            hasCoolerControl.value = newValue.hasCoolerControl
            isCoolerOn.value = newValue.isCoolerOn
            hasDewHeater.value = newValue.hasDewHeater
            isDewHeaterOn.value = newValue.isDewHeaterOn
            frameFormats.setAll(newValue.frameFormats)
            canAbort.value = newValue.canAbort
            cfaOffsetX.value = newValue.cfaOffsetX
            cfaOffsetY.value = newValue.cfaOffsetY
            cfaType.value = newValue.cfaType
            exposureMin.value = newValue.exposureMin
            exposureMax.value = newValue.exposureMax
            exposureState.value = newValue.exposureState
            exposure.value = newValue.exposure
            hasCooler.value = newValue.hasCooler
            canSetTemperature.value = newValue.canSetTemperature
            temperature.value = newValue.temperature
            canSubFrame.value = newValue.canSubFrame
            x.value = newValue.x
            minX.value = newValue.minX
            maxX.value = newValue.maxX
            y.value = newValue.y
            minY.value = newValue.minY
            maxY.value = newValue.maxY
            width.value = newValue.width
            minWidth.value = newValue.minWidth
            maxWidth.value = newValue.maxWidth
            height.value = newValue.height
            minHeight.value = newValue.minHeight
            maxHeight.value = newValue.maxHeight
            canBin.value = newValue.canBin
            maxBinX.value = newValue.maxBinX
            maxBinY.value = newValue.maxBinY
            binX.value = newValue.binX
            binY.value = newValue.binY
            gainMin.value = newValue.gainMin
            gainMax.value = newValue.gainMax
            gain.value = newValue.gain
            offsetMin.value = newValue.offsetMin
            offsetMax.value = newValue.offsetMax
            offset.value = newValue.offset
        }
    }

    fun reset() {
        isConnected.value = false
        hasCoolerControl.value = false
        isCoolerOn.value = false
        hasDewHeater.value = false
        isDewHeaterOn.value = false
        frameFormats.clear()
        canAbort.value = false
        cfaOffsetX.value = 0
        cfaOffsetY.value = 0
        cfaType.value = CfaPattern.RGGB
        exposureMin.value = 0L
        exposureMax.value = 0L
        exposureState.value = PropertyState.IDLE
        exposure.value = 0L
        hasCooler.value = false
        canSetTemperature.value = false
        temperature.value = 0.0
        canSubFrame.value = false
        x.value = 0
        minX.value = 0
        maxX.value = 0
        y.value = 0
        minY.value = 0
        maxY.value = 0
        width.value = 0
        minWidth.value = 0
        maxWidth.value = 0
        height.value = 0
        minHeight.value = 0
        maxHeight.value = 0
        canBin.value = false
        maxBinX.value = 1
        maxBinY.value = 1
        binX.value = 1
        binY.value = 1
        gainMin.value = 0
        gainMax.value = 0
        gain.value = 0
        offsetMin.value = 0
        offsetMax.value = 0
        offset.value = 0
    }

    override fun accept(event: Any) {
        if (event is DeviceEvent<*> && event.device === value) {
            Platform.runLater {
                when (event) {
                    is DeviceConnected,
                    is DeviceDisconnected -> isConnected.value = value.isConnected
                    is CameraCoolerControlChanged -> hasCoolerControl.value = value.hasCoolerControl
                    is CameraCoolerChanged -> isCoolerOn.value = value.isCoolerOn
                    is CameraHasDewHeaterChanged -> hasDewHeater.value = value.hasDewHeater
                    is CameraDewHeaterChanged -> isDewHeaterOn.value = value.isDewHeaterOn
                    is CameraFrameFormatsChanged -> frameFormats.setAll(value.frameFormats)
                    is CameraCanAbortChanged -> canAbort.value = value.canAbort
                    is CameraCfaChanged -> {
                        cfaOffsetX.value = value.cfaOffsetX
                        cfaOffsetY.value = value.cfaOffsetY
                        cfaType.value = value.cfaType
                    }
                    is CameraExposureMinMaxChanged -> {
                        exposureMin.value = value.exposureMin
                        exposureMax.value = value.exposureMax
                    }
                    is CameraGainChanged -> gain.value = value.gain
                    is CameraGainMinMaxChanged -> {
                        gainMin.value = value.gainMin
                        gainMax.value = value.gainMax
                    }
                    is CameraOffsetChanged -> offset.value = value.offset
                    is CameraOffsetMinMaxChanged -> {
                        offsetMin.value = value.offsetMin
                        offsetMax.value = value.offsetMax
                    }
                    is CameraExposureStateChanged -> exposureState.value = value.exposureState
                    is CameraHasCoolerChanged -> hasCooler.value = value.hasCooler
                    is CameraCanSetTemperatureChanged -> canSetTemperature.value = value.canSetTemperature
                    is CameraTemperatureChanged -> temperature.value = value.temperature
                    is CameraCanSubFrameChanged -> canSubFrame.value = value.canSubFrame
                    is CameraFrameChanged -> {
                        minX.value = value.minX
                        maxX.value = value.maxX
                        minY.value = value.minY
                        maxY.value = value.maxY
                        minWidth.value = value.minWidth
                        maxWidth.value = value.maxWidth
                        minHeight.value = value.minHeight
                        maxHeight.value = value.maxHeight
                        x.value = value.x
                        y.value = value.y
                        width.value = value.width
                        height.value = value.height
                    }
                    is CameraCanBinChanged -> {
                        canBin.value = value.canBin
                        maxBinX.value = value.maxBinX
                        maxBinY.value = value.maxBinY
                    }
                    is CameraBinChanged -> {
                        binX.value = value.binX
                        binY.value = value.binY
                    }
                }
            }
        }
    }
}
