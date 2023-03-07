package nebulosa.desktop.view.image

import nebulosa.desktop.view.View
import nom.tam.fits.Header

interface FitsHeaderView : View {

    fun updateText(text: String)

    fun load(header: Header)
}
