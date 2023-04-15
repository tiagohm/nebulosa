package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.TextArea
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.image.FitsHeaderManager
import nebulosa.desktop.view.image.FitsHeaderView
import nom.tam.fits.Header

class FitsHeaderWindow : AbstractWindow("FitsHeader", "text-box"), FitsHeaderView {

    @FXML private lateinit var cardsTextArea: TextArea

    private val fitsHeaderManager = FitsHeaderManager(this)

    init {
        title = "FITS Header"
    }

    override suspend fun updateText(text: String) = withMain {
        cardsTextArea.text = text
    }

    override suspend fun load(header: Header) = withMain {
        fitsHeaderManager.load(header)
    }
}
