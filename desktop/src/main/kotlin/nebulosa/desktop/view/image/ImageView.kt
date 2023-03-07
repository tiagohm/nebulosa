package nebulosa.desktop.view.image

import nebulosa.desktop.view.View
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import java.io.File

interface ImageView : View, Iterable<Drawable> {

    interface Opener {

        fun open(image: Image?, file: File?, token: Any? = null): ImageView
    }

    val camera: Camera?

    var hasScnr: Boolean

    val originalImage: Image?

    val transformedImage: Image?

    val shadow: Float

    val highlight: Float

    val midtone: Float

    val mirrorHorizontal: Boolean

    val mirrorVertical: Boolean

    val invert: Boolean

    val scnrEnabled: Boolean

    val scnrChannel: ImageChannel

    val scnrProtectionMode: ProtectionMethod

    val scnrAmount: Float

    var crosshairEnabled: Boolean

    val image
        get() = transformedImage ?: originalImage

    fun stf(shadow: Float, highlight: Float, midtone: Float)

    fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )

    fun adjustSceneToImage()

    fun draw(image: Image)

    fun open(file: File)

    fun open(fits: Image, file: File? = null)

    fun redraw()

    fun addFirst(element: Drawable)

    fun addLast(element: Drawable)

    fun remove(element: Drawable): Boolean

    fun removeFirst(): Drawable

    fun removeLast(): Drawable

    fun removeAll(elements: Collection<Drawable>): Boolean

    fun removeAll()
}
