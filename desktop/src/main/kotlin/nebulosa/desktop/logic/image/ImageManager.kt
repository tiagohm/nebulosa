package nebulosa.desktop.logic.image

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import javafx.stage.Screen
import nebulosa.desktop.gui.image.Crosshair
import nebulosa.desktop.gui.image.FitsHeaderWindow
import nebulosa.desktop.gui.image.ImageStretcherWindow
import nebulosa.desktop.gui.image.SCNRWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.platesolver.PlateSolverView
import nebulosa.fits.dec
import nebulosa.fits.ra
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.math.Angle.Companion.deg
import nebulosa.platesolving.Calibration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.io.Closeable
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class ImageManager(private val view: ImageView) : Closeable {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var plateSolverView: PlateSolverView

    @Volatile var file: File? = null
        private set

    @Volatile var fits: Image? = null
        private set

    @Volatile var transformedFits: Image? = null
        private set

    private val screenBounds = Screen.getPrimary().bounds
    private val transformPublisher = BehaviorSubject.create<Unit>()

    @Volatile private var transformSubscriber: Disposable? = null

    @Volatile private var imageStretcherWindow: ImageStretcherWindow? = null
    @Volatile private var fitsHeaderWindow: FitsHeaderWindow? = null
    @Volatile private var scnrWindow: SCNRWindow? = null

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
        val fits = Image.open(file)
        open(fits, file)
    }

    @Synchronized
    fun open(fits: Image, file: File? = null) {
        this.file = file

        updateTitle()

        val adjustSceneToImage = this.fits == null

        this.fits = fits
        this.transformedFits = null
        this.calibration.set(null)

        view.hasScnr = !fits.mono

        transformImage()
        draw()
        drawHistogram()

        imageStretcherWindow?.updateTitle()

        if (adjustSceneToImage) {
            view.adjustSceneToImage()
        }
    }

    fun draw() {
        val fits = transformedFits ?: fits ?: return
        view.draw(fits)
    }

    fun drawHistogram() {
        imageStretcherWindow?.drawHistogram()
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
        transformedFits = if (shouldBeTransformed) fits!!.clone() else null

        if (transformedFits != null) {
            val algorithms = arrayListOf<TransformAlgorithm>()

            algorithms.add(Flip(mirrorHorizontal, mirrorVertical))
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))

            if (invert) algorithms.add(Invert)

            transformedFits = TransformAlgorithm.of(algorithms).transform(transformedFits!!)
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
        if (view.crosshairEnabled) view.addFirst(Crosshair)
        else view.remove(Crosshair)

        view.redraw()
    }

    fun adjustSceneSizeToFitImage(defaultSize: Boolean = fits == null) {
        val fits = fits ?: return

        val factor = fits.width.toDouble() / fits.height.toDouble()

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
        val fits = fits ?: return false

        when (file.extension.lowercase()) {
            "png" -> ImageIO.write(fits, "PNG", file)
            "jpg", "jpeg" -> ImageIO.write(fits, "JPEG", file)
            "fit", "fits" -> fits.writeAsFits(file)
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
        val fits = fits ?: return false

        plateSolverView.show(bringToFront = true)

        val ra = fits.header.ra
        val dec = fits.header.dec

        val task = if (ra != null && dec != null) {
            LOG.info("plate solving. path={}, ra={}, dec={}", file, ra.hours, dec.degrees)
            plateSolverView.solve(file, false, ra, dec, 10.0.deg)
        } else {
            LOG.info("blind plate solving. path={}", file)
            plateSolverView.solve(file)
        }

        task.whenComplete { calibration, e ->
            this.calibration.set(calibration)

            if (calibration != null) {
                LOG.info("plate solving finished. calibration={}", calibration)
            } else if (e != null) {
                LOG.error("plate solving failed.", e)
            }
        }

        return true
    }

    fun openImageStretcher() {
        imageStretcherWindow = imageStretcherWindow ?: ImageStretcherWindow(view)
        imageStretcherWindow!!.show(bringToFront = true)
    }

    fun openSCNR() {
        scnrWindow = scnrWindow ?: SCNRWindow(view)
        scnrWindow!!.show(bringToFront = true)
    }

    fun openFitsHeader() {
        val header = fits?.header ?: return
        fitsHeaderWindow = fitsHeaderWindow ?: FitsHeaderWindow()
        fitsHeaderWindow!!.show(bringToFront = true)
        fitsHeaderWindow!!.load(header)
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
        if (view.camera != null) {
            preferences.double("image.${view.camera!!.name}.screen.x", view.x)
            preferences.double("image.${view.camera!!.name}.screen.y", view.y)
        } else {
            preferences.double("image.screen.x", view.x)
            preferences.double("image.screen.y", view.y)
        }
    }

    override fun close() {
        fits = null
        transformedFits = null

        imageStretcherWindow?.close()
        scnrWindow?.close()
        fitsHeaderWindow?.close()

        imageStretcherWindow = null
        fitsHeaderWindow = null
        scnrWindow = null

        savePreferences()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(ImageManager::class.java)
    }
}
