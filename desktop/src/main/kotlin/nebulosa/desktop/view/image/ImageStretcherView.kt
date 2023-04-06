package nebulosa.desktop.view.image

import nebulosa.desktop.view.View

interface ImageStretcherView : View {

    val shadow: Float

    val highlight: Float

    val midtone: Float

    suspend fun apply(shadow: Float, highlight: Float, midtone: Float)

    suspend fun updateTitle()

    suspend fun updateStretchParameters(shadow: Float, highlight: Float, midtone: Float)

    fun autoStretch()

    suspend fun resetStretch(onlyParameters: Boolean = false)

    suspend fun drawHistogram()
}
