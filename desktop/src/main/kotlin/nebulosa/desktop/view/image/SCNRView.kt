package nebulosa.desktop.view.image

import nebulosa.desktop.view.View
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod

interface SCNRView : View {

    val amount: Float

    val protectionMethod: ProtectionMethod

    val channel: ImageChannel

    val enabled: Boolean

    suspend fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    )
}
