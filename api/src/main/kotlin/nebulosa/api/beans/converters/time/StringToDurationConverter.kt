package nebulosa.api.beans.converters.time

import org.springframework.boot.convert.DurationStyle
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class StringToDurationConverter : Converter<String, Duration> {

    override fun convert(source: String): Duration? {
        val text = source.ifBlank { null } ?: return null

        return text.toLongOrNull()?.let { Duration.ofNanos(it * 1000L) }
            ?: DurationStyle.SIMPLE.parse(text, ChronoUnit.MICROS)
    }
}
