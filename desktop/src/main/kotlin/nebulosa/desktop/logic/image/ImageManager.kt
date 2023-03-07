package nebulosa.desktop.logic.image

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import javafx.stage.Screen
import nebulosa.desktop.gui.image.FitsHeaderWindow
import nebulosa.desktop.gui.image.ImageStretcherWindow
import nebulosa.desktop.gui.image.SCNRWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
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
import nebulosa.platesolving.Calibration
import nom.tam.fits.Header
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.Closeable
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class ImageManager(private val view: ImageView) : Closeable {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var plateSolverView: PlateSolverView
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var systemExecutorService: ExecutorService

    @Volatile var file: File? = null
        private set

    @Volatile var image: Image? = null
        private set

    @Volatile var transformedImage: Image? = null
        private set

    private val screenBounds = Screen.getPrimary().bounds
    private val transformPublisher = BehaviorSubject.create<Unit>()

    @Volatile private var transformSubscriber: Disposable? = null
    @Volatile private var imageStretcherView: ImageStretcherView? = null
    @Volatile private var fitsHeaderView: FitsHeaderView? = null
    @Volatile private var scnrView: SCNRView? = null
    @Volatile private var annotationEnabled = false
    @Volatile private var annotation: Annotation? = null

    @Volatile var shadow = 0f
        private set

    @Volatile var highlight = 1f
        private set

    @Volatile var midtone = 0.5f
        private set

    @Volatile var mirrorHorizontal = false
        private set

    @Volatile var mirrorVertical = false
        private set

    @Volatile var invert = false
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

    init {
        transformSubscriber = transformPublisher
            .debounce(500L, TimeUnit.MILLISECONDS)
            .subscribe {
                transformImage()

                draw()
                drawHistogram()
            }
    }

    private fun updateTitle() {
        view.title = "Image"
            .let { if (view.camera != null) "$it · ${view.camera!!.name}" else it }
            .let { if (file != null) "$it · ${file!!.name}" else it }
    }

    @Synchronized
    fun open(file: File) {
        val image = Image.open(file)
        open(image, file)
    }

    @Synchronized
    fun open(image: Image, file: File? = null) {
        this.file = file

        updateTitle()

        val adjustSceneToImage = this.image == null

        this.image = image
        this.transformedImage = null
        calibration.set(null)

        // TODO: Extract WCS/Calibration.
        // TODO: If annotation enable, generate new annotation.

        annotation?.also(view::remove)
        annotation = null
        annotationEnabled = false

        view.hasScnr = !image.mono

        transformImage()
        draw()
        drawHistogram()

        imageStretcherView?.updateTitle()

        if (adjustSceneToImage) {
            view.adjustSceneToImage()
        }
    }

    fun draw() {
        val image = transformedImage ?: image ?: return
        view.draw(image)
    }

    fun drawHistogram() {
        imageStretcherView?.drawHistogram()
    }

    fun transformImage(
        shadow: Float = this.shadow, highlight: Float = this.highlight, midtone: Float = this.midtone,
        mirrorHorizontal: Boolean = this.mirrorHorizontal, mirrorVertical: Boolean = this.mirrorVertical,
        invert: Boolean = this.invert,
        scnrEnabled: Boolean = this.scnrEnabled, scnrChannel: ImageChannel = this.scnrChannel,
        scnrProtectionMode: ProtectionMethod = this.scnrProtectionMode,
        scnrAmount: Float = this.scnrAmount,
    ) {
        this.shadow = shadow
        this.highlight = highlight
        this.midtone = midtone
        this.mirrorHorizontal = mirrorHorizontal
        this.mirrorVertical = mirrorVertical
        this.invert = invert
        this.scnrEnabled = scnrEnabled
        this.scnrChannel = scnrChannel
        this.scnrProtectionMode = scnrProtectionMode
        this.scnrAmount = scnrAmount

        transformPublisher.onNext(Unit)
    }

    @Synchronized
    private fun transformImage() {
        val shouldBeTransformed = shadow != 0f || highlight != 1f || midtone != 0.5f
                || mirrorHorizontal || mirrorVertical || invert
                || scnrEnabled

        // TODO: How to handle rotation transformation if data is copy but width/height is not?
        // TODO: Reason: Image will be rotated for each draw.
        transformedImage = if (shouldBeTransformed) image!!.clone() else null

        if (transformedImage != null) {
            val algorithms = ArrayList<TransformAlgorithm>(5)

            if (mirrorHorizontal) algorithms.add(HorizontalFlip)
            if (mirrorVertical) algorithms.add(VerticalFlip)
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
            if (invert) algorithms.add(Invert)

            transformedImage = TransformAlgorithm.of(algorithms).transform(transformedImage!!)
        }
    }

    fun mirrorHorizontal() {
        transformImage(mirrorHorizontal = !mirrorHorizontal)
    }

    fun mirrorVertical() {
        transformImage(mirrorVertical = !mirrorVertical)
    }

    fun invert() {
        transformImage(invert = !invert)
    }

    fun toggleCrosshair() {
        if (view.crosshairEnabled) view.addLast(Crosshair)
        else view.remove(Crosshair)

        view.redraw()
    }

    fun toggleAnnotation() {
        val calibration = calibration.get() ?: return

        annotationEnabled = !annotationEnabled

        if (annotationEnabled) {
            if (annotation == null) {
                systemExecutorService.submit {
                    try {
                        annotation = Annotation(calibration)
                        view.addFirst(annotation!!)
                        javaFXExecutorService.submit(view::redraw)
                    } catch (e: Throwable) {
                        LOG.error("annotation failed", e)
                    }
                }
            } else {
                view.addFirst(annotation!!)
                view.redraw()
            }
        } else if (annotation != null) {
            view.remove(annotation!!)
            view.redraw()
        }
    }

    fun toggleAnnotationOptions() {

    }

    fun adjustSceneSizeToFitImage(defaultSize: Boolean = image == null) {
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

        if (factor >= 1.0) {
            view.width = defaultWidth + view.borderSize
            view.height = defaultWidth / factor + view.titleHeight
        } else {
            view.height = defaultHeight + view.titleHeight
            view.width = defaultHeight * factor + view.borderSize
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

    fun plateSolve(): Boolean {
        val file = file ?: return false
        val image = image ?: return false

        plateSolverView.show(bringToFront = true)

        val ra = image.header.ra
        val dec = image.header.dec

        val task = if (ra != null && dec != null) {
            LOG.info("plate solving. path={}, ra={}, dec={}", file, ra.hours, dec.degrees)
            plateSolverView.solve(file, false, ra, dec)
        } else {
            LOG.info("blind plate solving. path={}", file)
            plateSolverView.solve(file)
        }

        task.whenComplete { calibration, e ->
            if (calibration != null) {
                image.header.populateWithCalibration(calibration)
                LOG.info("plate solving finished. calibration={}", calibration)
            } else if (e != null) {
                LOG.error("plate solving failed.", e)
            }

            this.calibration.set(calibration)
        }

        return true
    }

    fun openImageStretcher() {
        imageStretcherView = imageStretcherView ?: ImageStretcherWindow(view)
        imageStretcherView!!.show(bringToFront = true)
    }

    fun openSCNR() {
        scnrView = scnrView ?: SCNRWindow(view)
        scnrView!!.show(bringToFront = true)
    }

    fun openFitsHeader() {
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
            preferences.double("image.screen.x", view.x)
            preferences.double("image.screen.y", view.y)
        }
    }

    override fun close() {
        image = null
        transformedImage = null

        imageStretcherView?.close()
        scnrView?.close()
        fitsHeaderView?.close()

        imageStretcherView = null
        fitsHeaderView = null
        scnrView = null

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
            addValue("CD1_1", calibration.cd11, "")
            addValue("CD1_2", calibration.cd12, "")
            addValue("CD2_1", calibration.cd21, "")
            addValue("CD2_2", calibration.cd22, "")
        }
    }
}
