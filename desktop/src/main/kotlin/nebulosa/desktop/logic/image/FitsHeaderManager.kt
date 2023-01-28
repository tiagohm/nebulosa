package nebulosa.desktop.logic.image

import nebulosa.desktop.view.image.FitsHeaderView
import nom.tam.fits.Header
import nom.tam.fits.HeaderCard
import kotlin.math.max

class FitsHeaderManager(private val view: FitsHeaderView) {

    fun load(header: Header) {
        val maxWidth = IntArray(3)

        fun HeaderCard.formatCard(): String {
            val key = key.padStart(maxWidth[0])
            val value = (value ?: "null").padEnd(maxWidth[1])
            return "%s: %s / %s".format(key, value, comment)
        }

        val text = header
            .iterator()
            .asSequence()
            .filter { it.key != "END" }
            .onEach { maxWidth[0] = max(it.key.length, maxWidth[0]) }
            .onEach { maxWidth[1] = max(it.value?.length ?: 0, maxWidth[1]) }
            .onEach { maxWidth[2] = max(it.comment?.length ?: 0, maxWidth[2]) }
            .toList()
            .joinToString("\n") { it.formatCard() }

        view.updateText(text)
        view.width = maxWidth.sum() * 6.0
    }
}
