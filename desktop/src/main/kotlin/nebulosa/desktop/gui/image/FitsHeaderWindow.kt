package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.scene.control.TextArea
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.image.FitsHeaderManager
import nom.tam.fits.Header

class FitsHeaderWindow : AbstractWindow() {

    @FXML private lateinit var cardsTextArea: TextArea

    override val resourceName = "FitsHeader"

    override val icon = "nebulosa-fits-header"

    private val fitsHeaderManager = FitsHeaderManager(this)

    init {
        title = "FITS Header"
    }

    var text
        get() = cardsTextArea.text!!
        set(value) {
            cardsTextArea.text = value
        }

    fun load(header: Header) {
        fitsHeaderManager.load(header)
    }
}
