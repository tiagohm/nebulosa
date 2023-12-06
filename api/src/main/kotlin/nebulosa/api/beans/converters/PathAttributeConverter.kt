package nebulosa.api.beans.converters

import jakarta.persistence.AttributeConverter
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class PathAttributeConverter : AttributeConverter<Path, String> {

    override fun convertToDatabaseColumn(attribute: Path?) = attribute?.toString()

    override fun convertToEntityAttribute(dbData: String?) = dbData?.let(Path::of)
}
