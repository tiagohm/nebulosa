package nebulosa.desktop.core.util

import javafx.util.StringConverter

object BitDepthStringConverter : StringConverter<Int>() {

    override fun toString(bitDepth: Int?) = if (bitDepth == null) "-" else "$bitDepth bits"

    override fun fromString(text: String?) = null
}
