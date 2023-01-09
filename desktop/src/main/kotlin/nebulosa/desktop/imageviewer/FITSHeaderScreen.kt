package nebulosa.desktop.imageviewer

import javafx.fxml.FXML
import javafx.scene.control.TextArea
import nebulosa.desktop.core.scene.Screen
import nom.tam.fits.Header
import nom.tam.fits.HeaderCard
import kotlin.math.max

class FITSHeaderScreen : Screen("FITSHeader", "nebulosa-fits-header") {

    @FXML private lateinit var cards: TextArea

    init {
        title = "FITS Header"
    }

    fun load(header: Header) {
        val maxWidth = IntArray(3)

        fun HeaderCard.formatCard(): String {
            val key = key.padStart(maxWidth[0])
            val value = (value ?: "null").padEnd(maxWidth[1])
            return "%s: %s / %s".format(key, value, comment)
        }

        cards.text = header
            .iterator()
            .asSequence()
            .filter { it.key != "END" }
            .onEach { maxWidth[0] = max(it.key.length, maxWidth[0]) }
            .onEach { maxWidth[1] = max(it.value?.length ?: 0, maxWidth[1]) }
            .onEach { maxWidth[2] = max(it.comment?.length ?: 0, maxWidth[2]) }
            .toList()
            .joinToString("\n") { it.formatCard() }

        width = maxWidth.sum() * 6.0
    }
}
