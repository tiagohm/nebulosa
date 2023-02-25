package nebulosa.desktop.logic.camera

import javafx.beans.property.*
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.imaging.algorithms.CfaPattern
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.protocol.PropertyState

interface CameraProperty : DeviceProperty<Camera> {

    val exposuringProperty: SimpleBooleanProperty

    val hasCoolerControlProperty: SimpleBooleanProperty

    val coolerPowerProperty: SimpleDoubleProperty

    val coolerProperty: SimpleBooleanProperty

    val hasDewHeaterProperty: SimpleBooleanProperty

    val dewHeaterProperty: SimpleBooleanProperty

    val frameFormatsProperty: SimpleListProperty<String>

    val canAbortProperty: SimpleBooleanProperty

    val cfaOffsetXProperty: SimpleIntegerProperty

    val cfaOffsetYProperty: SimpleIntegerProperty

    val cfaTypeProperty: SimpleObjectProperty<CfaPattern>

    val exposureMinProperty: SimpleLongProperty

    val exposureMaxProperty: SimpleLongProperty

    val exposureStateProperty: SimpleObjectProperty<PropertyState>

    val hasCoolerProperty: SimpleBooleanProperty

    val canSetTemperatureProperty: SimpleBooleanProperty

    val temperatureProperty: SimpleDoubleProperty

    val canSubFrameProperty: SimpleBooleanProperty

    val xProperty: SimpleIntegerProperty

    val minXProperty: SimpleIntegerProperty

    val maxXProperty: SimpleIntegerProperty

    val yProperty: SimpleIntegerProperty

    val minYProperty: SimpleIntegerProperty

    val maxYProperty: SimpleIntegerProperty

    val widthProperty: SimpleIntegerProperty

    val minWidthProperty: SimpleIntegerProperty

    val maxWidthProperty: SimpleIntegerProperty

    val heightProperty: SimpleIntegerProperty

    val minHeightProperty: SimpleIntegerProperty

    val maxHeightProperty: SimpleIntegerProperty

    val canBinProperty: SimpleBooleanProperty

    val maxBinXProperty: SimpleIntegerProperty

    val maxBinYProperty: SimpleIntegerProperty

    val binXProperty: SimpleIntegerProperty

    val binYProperty: SimpleIntegerProperty

    val gainProperty: SimpleIntegerProperty

    val gainMinProperty: SimpleIntegerProperty

    val gainMaxProperty: SimpleIntegerProperty

    val offsetProperty: SimpleIntegerProperty

    val offsetMinProperty: SimpleIntegerProperty

    val offsetMaxProperty: SimpleIntegerProperty

    val pixelSizeXProperty: SimpleDoubleProperty

    val pixelSizeYProperty: SimpleDoubleProperty

    val exposuring
        get() = exposuringProperty.get()

    val hasCoolerControl
        get() = hasCoolerControlProperty.get()

    val cooler
        get() = coolerProperty.get()

    val hasDewHeater
        get() = hasDewHeaterProperty.get()

    val dewHeater
        get() = dewHeaterProperty.get()

    val frameFormats: List<String>
        get() = frameFormatsProperty.get()

    val canAbort
        get() = canAbortProperty.get()

    val cfaOffsetX
        get() = cfaOffsetXProperty.get()

    val cfaOffsetY
        get() = cfaOffsetYProperty.get()

    val cfaType: CfaPattern?
        get() = cfaTypeProperty.get()

    val exposureMin
        get() = exposureMinProperty.get()

    val exposureMax
        get() = exposureMaxProperty.get()

    val exposureState: PropertyState?
        get() = exposureStateProperty.get()

    val hasCooler
        get() = hasCoolerProperty.get()

    val canSetTemperature
        get() = canSetTemperatureProperty.get()

    val temperature
        get() = temperatureProperty.get()

    val canSubFrame
        get() = canSubFrameProperty.get()

    val x
        get() = xProperty.get()

    val minX
        get() = minXProperty.get()

    val maxX
        get() = maxXProperty.get()

    val y
        get() = yProperty.get()

    val minY
        get() = minYProperty.get()

    val maxY
        get() = maxYProperty.get()

    val width
        get() = widthProperty.get()

    val minWidth
        get() = minWidthProperty.get()

    val maxWidth
        get() = maxWidthProperty.get()

    val height
        get() = heightProperty.get()

    val minHeight
        get() = minHeightProperty.get()

    val maxHeight
        get() = maxHeightProperty.get()

    val canBin
        get() = canBinProperty.get()

    val maxBinX
        get() = maxBinXProperty.get()

    val maxBinY
        get() = maxBinYProperty.get()

    val binX
        get() = binXProperty.get()

    val binY
        get() = binYProperty.get()

    val gain
        get() = gainProperty.get()

    val gainMin
        get() = gainMinProperty.get()

    val gainMax
        get() = gainMaxProperty.get()

    val offset
        get() = offsetProperty.get()

    val offsetMin
        get() = offsetMinProperty.get()

    val offsetMax
        get() = offsetMaxProperty.get()

    val pixelSizeX
        get() = pixelSizeXProperty.get()

    val pixelSizeY
        get() = pixelSizeYProperty.get()
}
