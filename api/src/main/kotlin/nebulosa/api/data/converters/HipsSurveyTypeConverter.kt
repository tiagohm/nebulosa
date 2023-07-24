package nebulosa.api.data.converters

import nebulosa.api.data.enums.HipsSurveyType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class HipsSurveyTypeConverter : Converter<String, HipsSurveyType> {

    override fun convert(source: String): HipsSurveyType? {
        return try {
            HipsSurveyType.valueOf(source)
        } catch (e: IllegalArgumentException) {
            HipsSurveyType.entries.firstOrNull { it.hipsSurvey.id == source }
        }
    }
}
