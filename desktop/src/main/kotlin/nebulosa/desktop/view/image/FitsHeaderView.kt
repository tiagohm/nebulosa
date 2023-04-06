package nebulosa.desktop.view.image

import nebulosa.desktop.view.View
import nom.tam.fits.Header

interface FitsHeaderView : View {

    suspend fun updateText(text: String)

    suspend fun load(header: Header)
}
