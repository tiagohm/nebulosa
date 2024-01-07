package nebulosa.api.beans.converters.time

import org.springframework.boot.convert.DurationStyle
import org.springframework.boot.convert.DurationUnit
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class StringToDurationConverter : GenericConverter {

    override fun getConvertibleTypes() = SUPPORTED_TYPES

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        val text = (source as? String)?.ifBlank { null } ?: return null
        val unit = targetType.getAnnotation(DurationUnit::class.java)?.value ?: ChronoUnit.MICROS
        return convert(text, unit)
    }

    companion object {

        @JvmStatic
        private val SUPPORTED_TYPES = setOf(ConvertiblePair(String::class.java, Duration::class.java))

        @JvmStatic
        fun convert(source: String?, unit: ChronoUnit = ChronoUnit.MICROS): Duration? {
            val text = source?.ifBlank { null } ?: return null

            return text.toLongOrNull()
                ?.let { Duration.of(it, unit) }
                ?: DurationStyle.SIMPLE.parse(text, unit)
        }
    }
}
