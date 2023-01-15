package nebulosa.indi.protocol.io

import com.thoughtworks.xstream.converters.enums.EnumSingleValueConverter
import nebulosa.indi.protocol.HasText

internal class HasTextEnumConverter<T>(type: Class<T>) : EnumSingleValueConverter(type) where T : Enum<T>, T : HasText {

    private val textToEnum = type.enumConstants.associateBy { it.text }

    override fun toString(obj: Any?) = (obj as? HasText)?.text

    override fun fromString(str: String?) = if (str == null) null else textToEnum[str]
}
