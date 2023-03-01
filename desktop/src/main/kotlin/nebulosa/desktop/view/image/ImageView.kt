package nebulosa.desktop.view.image

import nebulosa.desktop.view.View
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera

interface ImageView : View, Iterable<Drawable> {

    val camera: Camera?

    var hasScnr: Boolean

    val fits: Image?

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

    fun stf(shadow: Float, highlight: Float, midtone: Float)

    fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )

    fun adjustSceneToImage()

    fun draw(fits: Image)

    fun redraw()

    fun addFirst(element: Drawable)

    fun addLast(element: Drawable)

    fun remove(element: Drawable): Boolean

    fun removeFirst(): Drawable

    fun removeLast(): Drawable

    fun removeAll(elements: Collection<Drawable>): Boolean

    fun removeAll()
}
