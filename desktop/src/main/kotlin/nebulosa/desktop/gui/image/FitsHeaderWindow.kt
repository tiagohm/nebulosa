package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.TextArea
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.image.FitsHeaderManager
import nebulosa.desktop.view.image.FitsHeaderView
import nom.tam.fits.Header

class FitsHeaderWindow : AbstractWindow("FitsHeader", "nebulosa-fits-header"), FitsHeaderView {

    @FXML private lateinit var cardsTextArea: TextArea

    private val fitsHeaderManager = FitsHeaderManager(this)

    init {
        title = "FITS Header"
    }

    override fun updateText(text: String) {
        cardsTextArea.text = text
    }

    fun load(header: Header) {
        fitsHeaderManager.load(header)
    }
}
