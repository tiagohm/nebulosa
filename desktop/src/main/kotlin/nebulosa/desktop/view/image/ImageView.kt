package nebulosa.desktop.view.image

import javafx.geometry.Bounds
import nebulosa.desktop.view.View
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.cameras.Camera

interface ImageView : View {

    val camera: Camera?

    var hasScnr: Boolean

    var hasFitsHeader: Boolean

    val imageBounds: Bounds

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

    var imageWidth: Double

    var imageHeight: Double

    fun applySTF(shadow: Float, highlight: Float, midtone: Float)

    fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )

    fun draw(
        fits: Image,
        width: Int, height: Int,
        startX: Int, startY: Int,
        factor: Float,
    )
}
