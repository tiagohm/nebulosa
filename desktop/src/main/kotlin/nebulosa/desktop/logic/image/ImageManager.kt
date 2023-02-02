package nebulosa.desktop.logic.image

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.stage.Screen
import nebulosa.desktop.gui.image.FitsHeaderWindow
import nebulosa.desktop.gui.image.ImageStretcherWindow
import nebulosa.desktop.gui.image.SCNRWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.view.image.ImageView
import nebulosa.imaging.ExtendedImage
import nebulosa.imaging.FitsImage
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nom.tam.fits.Fits
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class ImageManager(private val view: ImageView) : KoinComponent, Closeable {

    private val preferences by inject<Preferences>()

    @Volatile var fits: Image? = null
        private set

    @Volatile var transformedFits: Image? = null
        private set

    @Volatile var scale = 1f
        private set

    @Volatile private var scaleFactor = 0
    @Volatile private var startX = 0
    @Volatile private var startY = 0
    @Volatile private var dragging = false
    @Volatile private var dragStartX = 0.0
    @Volatile private var dragStartY = 0.0

    private val screenBounds = Screen.getPrimary().bounds
    private val transformPublisher = BehaviorSubject.create<Unit>()

    @Volatile private var idealSceneWidth = 640.0
    @Volatile private var idealSceneHeight = 640.0
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

    init {
        transformSubscriber = transformPublisher
            .debounce(500L, TimeUnit.MILLISECONDS)
            .subscribe {
                transformImage()

                draw()
                drawHistogram()
            }
    }

    @Synchronized
    fun open(file: File) {
        updateTitle(file)

        val adjustToDefaultSize = fits == null

        val fits = if (file.extension.startsWith("fit")) FitsImage(Fits(file))
        else ExtendedImage(file)

        this.fits = fits
        this.transformedFits = null

        view.hasScnr = !fits.mono
        view.hasFitsHeader = fits is FitsImage

        adjustSceneSizeToFitImage(adjustToDefaultSize)

        transformImage()

        draw()
        drawHistogram()

        imageStretcherWindow?.updateTitle()
    }

    fun draw() {
        val fits = transformedFits ?: fits ?: return

        val areaWidth = min(idealSceneWidth, screenBounds.width).toInt()
        val areaHeight = min(idealSceneHeight, screenBounds.height).toInt()

        val factorW = fits.width.toFloat() / areaWidth
        val factorH = fits.height.toFloat() / areaHeight
        val factor = max(factorW, factorH) / scale

        val maxStartX = (fits.width / factor).toInt()
        val maxStartY = (fits.height / factor).toInt()

        // Prevent move to left/up.
        if (-startX < 0 || -startY < 0) {
            if (maxStartX - startX <= view.sceneWidth.toInt()) {
                startX = maxStartX - view.sceneWidth.toInt()
            }
            if (maxStartY - startY <= view.sceneHeight.toInt()) {
                startY = maxStartY - view.sceneHeight.toInt()
            }
        }

        // Prevent move to right/bottom.
        if (startX > maxStartX) {
            startX = maxStartX
        } else if (startX < 0) {
            startX = 0
        }

        if (startY > maxStartY) {
            startY = maxStartY
        } else if (startY < 0) {
            startY = 0
        }

        view.draw(fits, areaWidth, areaHeight, startX, startY, factor)
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

    fun resetZoom() {
        startX = 0
        startY = 0
        scaleFactor = 0
        scale = 1f
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

    private fun updateTitle(file: File? = null) {
        view.title = "Image"
            .let { if (view.camera != null) "$it · ${view.camera!!.name}" else it }
            .let { if (file != null) "$it · ${file.name}" else it }
    }

    fun adjustSceneSizeToFitImage(defaultSize: Boolean) {
        val fits = fits ?: return

        val factor = fits.width.toDouble() / fits.height.toDouble()

        val defaultWidth = view.camera
            ?.let { preferences.double("image.${it.name}.screen.width") }
            ?: (screenBounds.width / 2)

        val defaultHeight = view.camera
            ?.let { preferences.double("image.${it.name}.screen.height") }
            ?: (screenBounds.height / 2)

        val borderSize = view.borderSize
        val titleHeight = view.titleHeight

        val sceneSize = if (factor >= 1.0)
            if (defaultSize) defaultWidth
            else min(screenBounds.width, view.width)
        else if (defaultSize) defaultHeight
        else min(screenBounds.height, view.height)

        if (factor >= 1.0) {
            idealSceneWidth = sceneSize
            idealSceneHeight = sceneSize / factor
        } else {
            idealSceneHeight = sceneSize
            idealSceneWidth = sceneSize * factor
        }

        view.width = idealSceneWidth + borderSize * 2
        view.height = idealSceneHeight + titleHeight

        view.imageWidth = idealSceneWidth
        view.imageHeight = idealSceneHeight
    }

    fun drag(event: MouseEvent) {
        if (!dragging) {
            dragging = true
            dragStartX = event.x
            dragStartY = event.y
        } else {
            val deltaX = (event.x - dragStartX).toInt()
            val deltaY = (event.y - dragStartY).toInt()

            startX -= deltaX
            startY -= deltaY

            startX = max(0, startX)
            startY = max(0, startY)

            dragStartX = event.x
            dragStartY = event.y

            draw()
        }
    }

    fun dragStop(event: MouseEvent) {
        dragging = false
    }

    fun zoomWithWheel(event: ScrollEvent) {
        val delta = if (event.deltaY == 0.0 && event.deltaX != 0.0) event.deltaX else event.deltaY
        val wheel = if (delta < 0) -1 else 1
        val scaleFactor = max(0, min(scaleFactor + wheel, SCALE_FACTORS.size - 1))

        if (scaleFactor != this.scaleFactor) {
            this.scaleFactor = scaleFactor
            val newScale = SCALE_FACTORS[scaleFactor]
            zoomToPoint(newScale, event.x, event.y)
        }
    }

    fun zoomToPoint(
        newScale: Double,
        pointX: Double, pointY: Double,
    ) {
        val bounds = view.imageBounds

        val x = ((startX + pointX) / bounds.width) * (bounds.width * newScale)
        val y = ((startY + pointY) / bounds.height) * (bounds.height * newScale)

        val toX = (x / newScale - x / scale + x * newScale) / newScale
        val toY = (y / newScale - y / scale + y * newScale) / newScale

        scale = newScale.toFloat()

        startX -= ((toX - x) * scale).toInt()
        startY -= ((toY - y) * scale).toInt()

        startX = max(0, startX)
        startY = max(0, startY)

        // val areaWidth = scene.width.toInt()
        // val areaHeight = scene.height.toInt()
        //
        // val factorW = fits!!.width.toFloat() / areaWidth
        // val factorH = fits!!.height.toFloat() / areaHeight
        // val factor = max(factorW, factorH) / scale
        //
        // val maxStartX = (fits!!.width / factor).toInt()
        // val maxStartY = (fits!!.height / factor).toInt()
        //
        // val x0 = -startX
        // val y0 = -startY
        //
        // val x1 = -startX + maxStartX
        // val y1 = -startY + maxStartY
        //
        // val px = image.localToParent(pointX, pointY)
        //
        // if (px.x >= x0 && px.x <= x1 && px.y >= y0 && px.y <= y1) {
        //     println("dentro $x0 $x1 $y0 $y1")
        // } else {
        //     println("fora")
        //     startX = 0
        //     startY = 0
        // }

        draw()
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
        val header = (fits as? FitsImage)?.header ?: return
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

        scale = 1f
        scaleFactor = 0
        startX = 0
        startY = 0
        dragging = false
        dragStartX = 0.0
        dragStartY = 0.0

        imageStretcherWindow?.close()
        scnrWindow?.close()
        fitsHeaderWindow?.close()

        imageStretcherWindow = null
        fitsHeaderWindow = null
        scnrWindow = null

        savePreferences()
    }

    companion object {

        @JvmStatic private val SCALE_FACTORS = doubleArrayOf(
            // 0.125, 0.25, 0.5, 0.75,
            1.0, 1.25, 1.5, 1.75, 2.0,
            3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
            10.0, 15.0, 20.0, 25.0, 50.0, 75.0, 100.0,
            200.0, 300.0, 400.0, 500.0,
        )
    }
}
