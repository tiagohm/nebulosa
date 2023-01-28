package nebulosa.desktop.view.image

import nebulosa.desktop.view.View

interface ImageStretcherView : View {

    var shadow: Float

    var highlight: Float

    var midtone: Float

    fun apply(shadow: Float, highlight: Float, midtone: Float)

    fun updateTitle()

    fun drawHistogram()
}
