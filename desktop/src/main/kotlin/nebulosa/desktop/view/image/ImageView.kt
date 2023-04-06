package nebulosa.desktop.view.image

import javafx.scene.Node
import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import java.io.File

interface ImageView : View {

    interface Opener {

        fun open(
            image: Image?, file: File?,
            token: Any? = null,
            resetTransformation: Boolean = false,
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

    fun stf(shadow: Float, highlight: Float, midtone: Float)

    fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )

    fun adjustSceneToImage()

    fun draw(image: Image)

    fun open(file: File, resetTransformation: Boolean = false)

    fun open(
        fits: Image, file: File? = null,
        resetTransformation: Boolean = false,
    )

    fun redraw()

    fun addFirst(shape: Node)

    fun addLast(shape: Node)

    fun remove(shape: Node)

    fun removeFirst(): Node?

    fun removeLast(): Node?

    fun registerMouseListener(listener: ImageViewer.MouseListener)

    fun unregisterMouseListener(listener: ImageViewer.MouseListener)
}
