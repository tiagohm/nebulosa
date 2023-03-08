package nebulosa.desktop.view.image

import nebulosa.desktop.view.View

interface ImageStretcherView : View {

    val shadow: Float

    val highlight: Float

    val midtone: Float

    fun apply(shadow: Float, highlight: Float, midtone: Float)

    fun updateTitle()

    fun updateStretchParameters(shadow: Float, highlight: Float, midtone: Float)

    fun autoStretch()

    fun resetStretch(onlyParameters: Boolean = false)

    fun drawHistogram()
}
