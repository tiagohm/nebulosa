package nebulosa.desktop.gui.guider

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.gui.control.MaterialIcon
import nebulosa.desktop.gui.control.SwitchSegmentedButton
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.guider.GuiderManager
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.guider.GuideAlgorithmType
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.guiding.GuideStats
import nebulosa.guiding.Guider
import nebulosa.guiding.internal.DeclinationGuideMode
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.AutoScreenTransformFunction
import nebulosa.imaging.algorithms.SubFrame
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.nio.IntBuffer
import kotlin.math.min

@Component
class GuiderWindow : AbstractWindow("Guider", "target"), GuiderView {

    @Lazy @Autowired private lateinit var guiderManager: GuiderManager

    @FXML private lateinit var guideCameraChoiceBox: ChoiceBox<Camera>
    @FXML private lateinit var connectGuideCameraButton: TwoStateButton
    @FXML private lateinit var openINDIForGuideCameraButton: Button
    @FXML private lateinit var guideMountChoiceBox: ChoiceBox<Mount>
    @FXML private lateinit var connectGuideMountButton: TwoStateButton
    @FXML private lateinit var openINDIForGuideMountButton: Button
    @FXML private lateinit var guideOutputChoiceBox: ChoiceBox<GuideOutput>
    @FXML private lateinit var connectGuideOutputButton: TwoStateButton
    @FXML private lateinit var openINDIForGuideOutputButton: Button
    @FXML private lateinit var startLoopingButton: Button
    @FXML private lateinit var stopLoopingButton: Button
    @FXML private lateinit var startGuidingButton: Button
    @FXML private lateinit var stopGuidingButton: Button
    @FXML private lateinit var statusIcon: MaterialIcon
    @FXML private lateinit var starProfileImage: ImageViewer
    @FXML private lateinit var starProfileGraph: StarProfileGraph
    @FXML private lateinit var peakLabel: Label
    @FXML private lateinit var fwhmLabel: Label
    @FXML private lateinit var hfdLabel: Label
    @FXML private lateinit var snrLabel: Label
    @FXML private lateinit var guiderChart: GuiderChart
    @FXML private lateinit var rmsRALabel: Label
    @FXML private lateinit var rmsDECLabel: Label
    @FXML private lateinit var rmsTotalLabel: Label
    @FXML private lateinit var guiderChartTicks: GuiderChartTicks
    @FXML private lateinit var algorithmRAChoiceBox: ChoiceBox<GuideAlgorithmType>
    @FXML private lateinit var maxDurationRASpinner: Spinner<Double>
    @FXML private lateinit var hysteresisRASpinner: Spinner<Double>
    @FXML private lateinit var aggressivenessRASpinner: Spinner<Double>
    @FXML private lateinit var minimumMoveRASpinner: Spinner<Double>
    @FXML private lateinit var slopeWeightRASpinner: Spinner<Double>
    @FXML private lateinit var fastSwitchForLargeDeflectionsRASwitch: SwitchSegmentedButton
    @FXML private lateinit var algorithmDECChoiceBox: ChoiceBox<GuideAlgorithmType>
    @FXML private lateinit var maxDurationDECSpinner: Spinner<Double>
    @FXML private lateinit var guideModeDECChoiceBox: ChoiceBox<DeclinationGuideMode>
    @FXML private lateinit var hysteresisDECSpinner: Spinner<Double>
    @FXML private lateinit var aggressivenessDECSpinner: Spinner<Double>
    @FXML private lateinit var minimumMoveDECSpinner: Spinner<Double>
    @FXML private lateinit var slopeWeightDECSpinner: Spinner<Double>
    @FXML private lateinit var fastSwitchForLargeDeflectionsDECSwitch: SwitchSegmentedButton
    @FXML private lateinit var calibrationStepSpinner: Spinner<Double>
    @FXML private lateinit var assumeDECOrthogonalToRASwitch: SwitchSegmentedButton
    @FXML private lateinit var useDECCompensationSwitch: SwitchSegmentedButton

    private val starProfileData = IntArray(64 * 64)
    private val starProfileIndicator = StarProfileIndicator()

    init {
        title = "Guider"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !guiderManager.selectedGuideCamera.connectedProperty or
                !guiderManager.selectedGuideMount.connectedProperty or !guiderManager.selectedGuideOutput.connectedProperty
        val isConnecting = guiderManager.selectedGuideCamera.connectingProperty or
                guiderManager.selectedGuideMount.connectingProperty or guiderManager.selectedGuideOutput.connectingProperty
        val isLooping = guiderManager.loopingProperty
        val isGuiding = guiderManager.guidingProperty

        guiderManager.initialize()

        guideCameraChoiceBox.converter = GuideCameraStringConverter
        guideCameraChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guideCameraChoiceBox.itemsProperty().bind(guiderManager.cameras)
        guideCameraChoiceBox.selectionModel.selectedItemProperty().on { guiderManager.selectedGuideCamera.set(it) }

        connectGuideCameraButton.disableProperty().bind(guiderManager.selectedGuideCamera.isNull or isConnecting or isGuiding)
        guiderManager.selectedGuideCamera.connectedProperty.on { connectGuideCameraButton.state = it }

        openINDIForGuideCameraButton.disableProperty().bind(connectGuideCameraButton.disableProperty())

        guideMountChoiceBox.converter = GuideMountStringConverter
        guideMountChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guideMountChoiceBox.itemsProperty().bind(guiderManager.mounts)
        guideMountChoiceBox.selectionModel.selectedItemProperty().on { guiderManager.selectedGuideMount.set(it) }

        connectGuideMountButton.disableProperty().bind(guiderManager.selectedGuideMount.isNull or isConnecting or isGuiding)
        guiderManager.selectedGuideMount.connectedProperty.on { connectGuideMountButton.state = it }

        openINDIForGuideMountButton.disableProperty().bind(connectGuideMountButton.disableProperty())

        guideOutputChoiceBox.converter = GuideOutputStringConverter
        guideOutputChoiceBox.disableProperty().bind(isConnecting or isGuiding)
        guideOutputChoiceBox.itemsProperty().bind(guiderManager.guideOutputs)
        guideOutputChoiceBox.selectionModel.selectedItemProperty().on { guiderManager.selectedGuideOutput.set(it) }

        connectGuideOutputButton.disableProperty().bind(guiderManager.selectedGuideOutput.isNull or isConnecting or isGuiding)
        guiderManager.selectedGuideOutput.connectedProperty.on { connectGuideOutputButton.state = it }

        openINDIForGuideOutputButton.disableProperty().bind(connectGuideOutputButton.disableProperty())

        startLoopingButton.disableProperty().bind(isNotConnected or isLooping)
        stopLoopingButton.disableProperty().bind(isNotConnected or !isLooping)

        startGuidingButton.disableProperty().bind(isNotConnected or !isLooping or isGuiding)
        stopGuidingButton.disableProperty().bind(isNotConnected or !isLooping or !isGuiding)

        starProfileImage.addFirst(starProfileIndicator)

        algorithmRAChoiceBox.converter = GuideAlgorithmTypeStringConverter
        algorithmRAChoiceBox.value = GuideAlgorithmType.HYSTERESIS

        algorithmDECChoiceBox.converter = GuideAlgorithmTypeStringConverter
        algorithmDECChoiceBox.value = GuideAlgorithmType.HYSTERESIS
    }

    @FXML
    private fun connectGuideCamera() {
        guiderManager.connectGuideCamera()
    }

    @FXML
    private fun connectGuideMount() {
        guiderManager.connectGuideMount()
    }

    @FXML
    private fun connectGuideOutput() {
        guiderManager.connectGuideOutput()
    }

    @FXML
    private fun openINDIForGuideCamera() {
        guiderManager.openINDIPanelControlForGuideCamera()
    }

    @FXML
    private fun openINDIForGuideMount() {
        guiderManager.openINDIPanelControlForGuideMount()
    }

    @FXML
    private fun openINDIForGuideOutput() {
        guiderManager.openINDIPanelControlForGuideOutput()
    }

    @FXML
    private fun startLooping() {
        guiderManager.startLooping()
    }

    @FXML
    private fun stopLooping() {
        guiderManager.stopLooping()
    }

    @FXML
    private fun startGuiding(event: MouseEvent) {
        guiderManager.startGuiding(event.isShiftDown)
    }

    @FXML
    private fun stopGuiding() {
        guiderManager.stopGuiding()
    }

    @FXML
    private fun changeGuideChartScale(event: ScrollEvent) {
        val delta = if (event.deltaY == 0.0 && event.deltaX != 0.0) event.deltaX else event.deltaY

        if (delta > 0) guiderChartTicks.incrementScale()
        else if (delta < 0) guiderChartTicks.decrementScale()
        else return

        guiderChart.changeScale(guiderChartTicks.scale.toDouble())
    }

    override val algorithmRA
        get() = algorithmRAChoiceBox.value ?: GuideAlgorithmType.HYSTERESIS

    override val maxDurationRA
        get() = maxDurationRASpinner.value!!.toInt()

    override val hysteresisRA
        get() = hysteresisRASpinner.value!! / 100.0

    override val aggressivenessRA
        get() = aggressivenessRASpinner.value!! / 100.0

    override val minimumMoveRA
        get() = minimumMoveRASpinner.value!!

    override val slopeWeightRA
        get() = slopeWeightRASpinner.value!!

    override val fastSwitchForLargeDeflectionsRA
        get() = fastSwitchForLargeDeflectionsRASwitch.state

    override val algorithmDEC
        get() = algorithmDECChoiceBox.value ?: GuideAlgorithmType.HYSTERESIS

    override val maxDurationDEC
        get() = maxDurationDECSpinner.value!!.toInt()

    override val guideModeDEC
        get() = guideModeDECChoiceBox.value ?: DeclinationGuideMode.AUTO

    override val hysteresisDEC
        get() = hysteresisDECSpinner.value!! / 100.0

    override val aggressivenessDEC
        get() = aggressivenessDECSpinner.value!! / 100.0

    override val minimumMoveDEC
        get() = minimumMoveDECSpinner.value!!

    override val slopeWeightDEC
        get() = slopeWeightDECSpinner.value!!

    override val fastSwitchForLargeDeflectionsDEC
        get() = fastSwitchForLargeDeflectionsDECSwitch.state

    override val calibrationStep
        get() = calibrationStepSpinner.value!!.toInt()

    override val assumeDECOrthogonalToRA
        get() = assumeDECOrthogonalToRASwitch.state

    override val useDECCompensation
        get() = useDECCompensationSwitch.state

    override fun updateStatus(text: String) {
        javaFXExecutorService.execute { statusIcon.text = text }
    }

    override fun updateStarProfile(guider: Guider, image: Image) {
        val lockPosition = guider.lockPosition
        val regionSize = guider.searchRegion * 2.0

        if (lockPosition.valid) {
            val size = min(regionSize, 64.0)

            systemExecutorService.submit {
                val centerX = (lockPosition.x - size / 2).toInt()
                val centerY = (lockPosition.y - size / 2).toInt()
                val profileImage = image.transform(SubFrame(centerX, centerY, size.toInt(), size.toInt()), AutoScreenTransformFunction)

                profileImage.writeTo(starProfileData)

                val buffer = IntBuffer.wrap(starProfileData)
                val pixelBuffer = PixelBuffer(profileImage.width, profileImage.height, buffer, PixelFormat.getIntArgbPreInstance())
                val writableImage = WritableImage(pixelBuffer)

                javaFXExecutorService.execute {
                    starProfileImage.load(writableImage)
                    starProfileIndicator.draw(guider.lockPosition, guider.primaryStar, regionSize)
                    val fwhm = starProfileGraph.draw(image, guider.primaryStar)
                    peakLabel.text = "%.0f".format(guider.primaryStar.peak)
                    fwhmLabel.text = "%.2f".format(fwhm)
                    hfdLabel.text = "%.2f".format(guider.primaryStar.hfd)
                    snrLabel.text = "%.2f".format(guider.primaryStar.snr)
                }
            }
        }
    }

    override fun updateGraph(
        stats: List<GuideStats>,
        maxRADuration: Double, maxDECDuration: Double,
    ) {
        javaFXExecutorService.execute { guiderChart.draw(stats, maxRADuration, maxDECDuration) }
    }

    override fun updateGraphInfo(rmsRA: Double, rmsDEC: Double, rmsTotal: Double, pixelScale: Double) {
        javaFXExecutorService.execute {
            rmsRALabel.text = "%.2f px | %.2f\"".format(rmsRA, rmsRA * pixelScale)
            rmsDECLabel.text = "%.2f px | %.2f\"".format(rmsDEC, rmsDEC * pixelScale)
            rmsTotalLabel.text = "%.2f px | %.2f\"".format(rmsTotal, rmsTotal * pixelScale)
        }
    }

    override fun onMouseClicked(
        button: MouseButton,
        clickCount: Int,
        isControlDown: Boolean, isShiftDown: Boolean, isAltDown: Boolean,
        mouseX: Double, mouseY: Double,
        imageX: Double, imageY: Double,
    ) {
        if (button == MouseButton.PRIMARY && clickCount == 1) {
            if (!isControlDown && !isShiftDown && !isAltDown) {
                guiderManager.selectGuideStar(imageX, imageY)
            } else if (!isControlDown && isShiftDown && !isAltDown) {
                guiderManager.deselectGuideStar()
            }
        }
    }

    private object GuideCameraStringConverter : StringConverter<Camera>() {

        override fun toString(device: Camera?) = device?.name ?: "No guiding camera selected"

        override fun fromString(text: String?) = null
    }

    private object GuideMountStringConverter : StringConverter<Mount>() {

        override fun toString(device: Mount?) = device?.name ?: "No guiding mount selected"

        override fun fromString(text: String?) = null
    }

    private object GuideOutputStringConverter : StringConverter<GuideOutput>() {

        override fun toString(device: GuideOutput?) = device?.name ?: "No guiding output selected"

        override fun fromString(text: String?) = null
    }

    private object GuideAlgorithmTypeStringConverter : StringConverter<GuideAlgorithmType>() {

        override fun toString(device: GuideAlgorithmType?) = device?.label ?: "No algorithm selected"

        override fun fromString(text: String?) = null
    }
}
