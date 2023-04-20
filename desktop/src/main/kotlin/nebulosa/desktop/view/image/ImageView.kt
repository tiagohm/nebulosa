package nebulosa.desktop.view.image

import javafx.scene.Node
import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import nebulosa.platesolving.Calibration
import java.io.File

interface ImageView : View {

    interface Opener {

        suspend fun open(
            image: Image?, file: File?,
            token: Any? = null,
            resetTransformation: Boolean = false,
            calibration: Calibration? = null,
        ): ImageView
    }

    val camera: Camera?

    val autoStretchEnabled: Boolean

    var hasScnr: Boolean

    val originalImage: Image?

    val transformedImage: Image?

    val shadow: Float

    val highlight: Float

    val midtone: Float

    var mirrorHorizontal: Boolean

    var mirrorVertical: Boolean

    var invert: Boolean

    val scnrEnabled: Boolean

    val scnrChannel: ImageChannel

    val scnrProtectionMode: ProtectionMethod

    val scnrAmount: Float

    val crosshairEnabled: Boolean

    val annotationEnabled: Boolean

    val image
        get() = transformedImage ?: originalImage

    suspend fun stf(shadow: Float, highlight: Float, midtone: Float)

    suspend fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )

    suspend fun adjustSceneToImage()

    suspend fun draw(image: Image)

    suspend fun open(
        file: File,
        resetTransformation: Boolean = false,
        calibration: Calibration? = null,
    )

    suspend fun open(
        fits: Image, file: File? = null,
        resetTransformation: Boolean = false,
        calibration: Calibration? = null,
    )

    fun transformAndDraw()

    fun redraw()

    fun addFirst(shape: Node)

    fun addLast(shape: Node)

    fun remove(shape: Node)

    fun removeFirst(): Node?

    fun removeLast(): Node?

    fun registerMouseListener(listener: ImageViewer.MouseListener)

    fun unregisterMouseListener(listener: ImageViewer.MouseListener)
}
