package nebulosa.desktop.logic.image

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Screen
import nebulosa.desktop.gui.control.Annotation
import nebulosa.desktop.gui.control.Crosshair
import nebulosa.desktop.gui.image.FitsHeaderWindow
import nebulosa.desktop.gui.image.ImageStretcherWindow
import nebulosa.desktop.gui.image.SCNRWindow
import nebulosa.desktop.helper.runBlockingMain
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.platesolver.PlateSolvingEvent
import nebulosa.desktop.logic.platesolver.PlateSolvingSolved
import nebulosa.desktop.repository.SkyObjectRepository
import nebulosa.desktop.view.image.FitsHeaderView
import nebulosa.desktop.view.image.ImageStretcherView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.image.SCNRView
import nebulosa.desktop.view.platesolver.PlateSolverView
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.indi.device.mount.Mount
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.SkyObject
import nebulosa.wcs.WCSTransform
import nom.tam.fits.Header
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

class ImageManager(private val view: ImageView) : AbstractManager(), Annotation.EventListener {

    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var plateSolverView: PlateSolverView
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var skyObjectRepository: SkyObjectRepository

    val file = SimpleObjectProperty<File>()

    @Volatile var image: Image? = null
        private set

    @Volatile var transformedImage: Image? = null
        private set

    private val screenBounds = Screen.getPrimary().bounds
    private val crosshair = Crosshair()
    private val annotation = Annotation()

    @Volatile private var imageStretcherView: ImageStretcherView? = null
    @Volatile private var fitsHeaderView: FitsHeaderView? = null
    @Volatile private var scnrView: SCNRView? = null

    val mountProperty
        get() = equipmentManager.selectedMount

    val mount: Mount?
        get() = mountProperty.value

    @Volatile var shadow = 0f
        private set

    @Volatile var highlight = 1f
        private set

    @Volatile var midtone = 0.5f
        private set

    @Volatile var scnrEnabled = false
        private set

    @Volatile var scnrChannel = ImageChannel.GREEN
        private set

    @Volatile var scnrProtectionMode = ProtectionMethod.AVERAGE_NEUTRAL
        private set

    @Volatile var scnrAmount = 0.5f
        private set

    val calibration = SimpleObjectProperty<Calibration>()

    fun initialize() {
        eventBus.register(this)

        crosshair.isVisible = false
        annotation.isVisible = false

        annotation.add(skyObjectRepository::searchStar, Color.YELLOW)
        annotation.add(skyObjectRepository::searchDSO, Color.LIGHTGREEN)
        annotation.registerEventListener(this)

        view.addFirst(crosshair)
        view.addFirst(annotation)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onPlateSolvingEvent(event: PlateSolvingEvent) = runBlockingMain {
        if (event.file === file.get()) {
            with(if (event is PlateSolvingSolved) event.calibration else null) {
                calibration.set(this)

                if (this != null) {
                    annotation.drawAround(this)
                    annotation.isVisible = view.annotationEnabled
                } else {
                    annotation.isVisible = false
                }
            }
        }
    }

    override fun onStarClicked(star: SkyObject) {
        view.showStarInfo(star)
    }

    private suspend fun updateTitle(text: String? = null) = withMain {
        view.title = if (text.isNullOrEmpty()) "Image"
            .let { if (view.camera != null) "$it · ${view.camera!!.name}" else it }
            .let { if (file.get() != null) "$it · ${file.get().name}" else it }
        else "Image · $text"
    }

    suspend fun open(
        file: File,
        resetTransformation: Boolean = false,
        calibration: Calibration? = null,
        title: String? = null,
    ) = withIO {
        val image = Image.open(file)
        open(image, file, resetTransformation, calibration, title)
    }

    suspend fun open(
        image: Image, file: File? = null,
        resetTransformation: Boolean = false,
        calibration: Calibration? = null,
        title: String? = null,
    ) {
        this.file.set(file)

        updateTitle(title)

        val adjustSceneToImage = this.image == null

        this.image = image
        this.transformedImage = null

        this.calibration.set(calibration)

        if (calibration != null) annotation.drawAround(calibration)

        withMain {
            annotation.isVisible = calibration != null && view.annotationEnabled
            view.hasScnr = !image.mono
        }

        if (resetTransformation) {
            shadow = 0f
            highlight = 1f
            midtone = 0.5f
            view.mirrorHorizontal = false
            view.mirrorVertical = false
            view.invert = false
            scnrEnabled = false

            imageStretcherView?.resetStretch(true)
        }

        if (view.autoStretchEnabled) autoStretch()
        else transformAndDraw()

        imageStretcherView?.updateTitle()

        if (adjustSceneToImage) {
            view.adjustSceneToImage()
        }
    }

    suspend fun draw() {
        val image = transformedImage ?: image ?: return
        view.draw(image)
    }

    suspend fun drawHistogram() {
        imageStretcherView?.drawHistogram()
    }

    fun transformImage(
        shadow: Float = this.shadow, highlight: Float = this.highlight, midtone: Float = this.midtone,
        mirrorHorizontal: Boolean = view.mirrorHorizontal, mirrorVertical: Boolean = view.mirrorVertical,
        invert: Boolean = view.invert,
        scnrEnabled: Boolean = this.scnrEnabled, scnrChannel: ImageChannel = this.scnrChannel,
        scnrProtectionMode: ProtectionMethod = this.scnrProtectionMode,
        scnrAmount: Float = this.scnrAmount,
    ) {
        this.shadow = shadow
        this.highlight = highlight
        this.midtone = midtone
        view.mirrorHorizontal = mirrorHorizontal
        view.mirrorVertical = mirrorVertical
        view.invert = invert
        this.scnrEnabled = scnrEnabled
        this.scnrChannel = scnrChannel
        this.scnrProtectionMode = scnrProtectionMode
        this.scnrAmount = scnrAmount

        view.transformAndDraw()
    }

    @Synchronized
    private fun transformImage() {
        val image = image ?: return

        val shouldBeTransformed = shadow != 0f || highlight != 1f || midtone != 0.5f
                || view.mirrorHorizontal || view.mirrorVertical || view.invert
                || scnrEnabled

        // TODO: How to handle rotation transformation if data is copy but width/height is not?
        // TODO: Reason: Image will be rotated for each draw.
        transformedImage = if (shouldBeTransformed) image.clone() else null

        if (transformedImage != null) {
            val algorithms = ArrayList<TransformAlgorithm>(5)

            if (view.mirrorHorizontal) algorithms.add(HorizontalFlip)
            if (view.mirrorVertical) algorithms.add(VerticalFlip)
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
            if (view.invert) algorithms.add(Invert)

            transformedImage = TransformAlgorithm.of(algorithms).transform(transformedImage!!)
        }
    }

    suspend fun transformAndDraw() = withIO {
        transformImage()
        draw()
        drawHistogram()
    }

    fun mirrorHorizontal() {
        transformImage(mirrorHorizontal = view.mirrorHorizontal)
    }

    fun mirrorVertical() {
        transformImage(mirrorVertical = view.mirrorVertical)
    }

    fun invert() {
        transformImage(invert = view.invert)
    }

    suspend fun toggleCrosshair() = withMain {
        crosshair.isVisible = !crosshair.isVisible
    }

    suspend fun toggleAnnotation() = withMain {
        annotation.isVisible = view.annotationEnabled
    }

    fun toggleAnnotationOptions() {}

    fun pointMountHere(x: Double, y: Double) {
        val mount = mount ?: return
        val calibration = calibration.get() ?: return
        val wcs = WCSTransform(calibration)
        val (rightAscension, declination) = wcs.pixelToWorld(x, y)
        val raOffset = calibration.rightAscension - mount.rightAscensionJ2000
        val decOffset = calibration.declination - mount.declinationJ2000
        mount.goToJ2000(rightAscension + raOffset, declination + decOffset)
    }

    suspend fun adjustSceneSizeToFitImage(defaultSize: Boolean = image == null) {
        val image = image ?: return

        val factor = image.width.toDouble() / image.height.toDouble()

        val defaultWidth = if (defaultSize) view.camera
            ?.let { preferences.double("image.${it.name}.screen.width") }
            ?: (screenBounds.width / 2)
        else view.width - view.borderSize

        val defaultHeight = if (defaultSize) view.camera
            ?.let { preferences.double("image.${it.name}.screen.height") }
            ?: (screenBounds.height / 2)
        else view.height - view.titleHeight

        withMain {
            if (factor >= 1.0) {
                view.width = defaultWidth + view.borderSize
                view.height = defaultWidth / factor + view.titleHeight
            } else {
                view.height = defaultHeight + view.titleHeight
                view.width = defaultHeight * factor + view.borderSize
            }
        }
    }

    private fun writeToFile(file: File): Boolean {
        val image = image ?: return false

        when (file.extension.lowercase()) {
            "png" -> ImageIO.write(image, "PNG", file)
            "jpg", "jpeg" -> ImageIO.write(image, "JPEG", file)
            "fit", "fits" -> image.writeAsFits(file)
            else -> return false
        }

        return true
    }

    fun save() {
        with(FileChooser()) {
            title = "Save Image"

            val imageSavePath = preferences.string("image.savePath")
            if (!imageSavePath.isNullOrBlank()) initialDirectory = File(imageSavePath)

            extensionFilters.add(FileChooser.ExtensionFilter("FITS", "*.fits", "*.fit"))
            extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
            extensionFilters.add(FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"))

            val file = showSaveDialog(null) ?: return

            if (!writeToFile(file)) {
                return view.showAlert("Unsupported format: ${file.extension}", "Save Error")
            }

            preferences.string("image.savePath", file.parent)
        }
    }

    suspend fun solve(blind: Boolean = false): Boolean {
        val file = file.get() ?: return false
        val image = image ?: return false

        plateSolverView.show(bringToFront = true)

        val ra = image.header.ra
        val dec = image.header.dec

        val calibration = if (!blind && ra != null && dec != null) {
            LOG.info("plate solving. path={}, ra={}, dec={}", file, ra.hours, dec.degrees)
            plateSolverView.solve(file, false, ra, dec)
        } else {
            LOG.info("blind plate solving. path={}", file)
            plateSolverView.solve(file)
        }

        if (calibration != null) {
            image.header.populateWithCalibration(calibration)
            LOG.info("plate solving finished. calibration={}", calibration)
        }

        this.calibration.set(calibration)

        return true
    }

    fun openImageStretcher() {
        imageStretcherView = imageStretcherView ?: ImageStretcherWindow(view)
        imageStretcherView!!.show(bringToFront = true)
    }

    suspend fun autoStretch() {
        if (view.autoStretchEnabled) {
            val params = AutoScreenTransformFunction.compute(image ?: return)
            imageStretcherView?.updateStretchParameters(params.shadow, params.highlight, params.midtone)
            view.stf(params.shadow, params.highlight, params.midtone)
        }
    }

    fun openSCNR() {
        scnrView = scnrView ?: SCNRWindow(view)
        scnrView!!.show(bringToFront = true)
    }

    suspend fun openFitsHeader() {
        val header = image?.header ?: return
        fitsHeaderView = fitsHeaderView ?: FitsHeaderWindow()
        fitsHeaderView!!.show(bringToFront = true)
        fitsHeaderView!!.load(header)
    }

    fun loadPreferences() {
        if (view.camera != null) {
            preferences.double("image.${view.camera!!.name}.screen.x")?.let { view.x = it }
            preferences.double("image.${view.camera!!.name}.screen.y")?.let { view.y = it }
        } else {
            preferences.double("image.screen.x")?.let { view.x = it }
            preferences.double("image.screen.y")?.let { view.y = it }
        }
    }

    fun savePreferences() {
        if (!view.initialized) return

        if (view.camera != null) {
            preferences.double("image.${view.camera!!.name}.screen.x", view.x)
            preferences.double("image.${view.camera!!.name}.screen.y", view.y)
        } else {
            preferences.double("image.screen.x", max(0.0, view.x))
            preferences.double("image.screen.y", max(0.0, view.y))
        }
    }

    override fun close() {
        if (image == null) return

        super.close()

        image = null
        transformedImage = null

        imageStretcherView?.close()
        scnrView?.close()
        fitsHeaderView?.close()

        imageStretcherView = null
        fitsHeaderView = null
        scnrView = null

        eventBus.unregister(this)

        savePreferences()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(ImageManager::class.java)

        @JvmStatic
        fun Header.populateWithCalibration(calibration: Calibration) {
            if (!calibration.hasWCS) return

            addValue("CTYPE1", calibration.ctype1, "")
            addValue("CTYPE2", calibration.ctype2, "")
            addValue("CRPIX1", calibration.crpix1, "")
            addValue("CRPIX2", calibration.crpix2, "")
            addValue("CRVAL1", calibration.crval1.degrees, "")
            addValue("CRVAL2", calibration.crval2.degrees, "")
            addValue("CDELT1", calibration.cdelt1.degrees, "")
            addValue("CDETL2", calibration.cdelt2.degrees, "")
            addValue("CROTA1", calibration.crota1.degrees, "")
            addValue("CROTA2", calibration.crota2.degrees, "")

            if (calibration.hasCD) {
                addValue("CD1_1", calibration.cd11, "")
                addValue("CD1_2", calibration.cd12, "")
                addValue("CD2_1", calibration.cd21, "")
                addValue("CD2_2", calibration.cd22, "")
            }

            if (calibration.hasPC) {
                addValue("PC1_1", calibration.pc11, "")
                addValue("PC1_2", calibration.pc12, "")
                addValue("PC2_1", calibration.pc21, "")
                addValue("PC2_2", calibration.pc22, "")
            }

            if (calibration.pv11 != null) addValue("PV1_1", calibration.pv11!!.degrees, "")
            if (calibration.pv12 != null) addValue("PV1_2", calibration.pv12!!.degrees, "")
        }
    }
}
