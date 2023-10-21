package nebulosa.api.beans.converters

import jakarta.persistence.AttributeConverter
import nebulosa.json.FromJson
import nebulosa.json.ToJson
import nebulosa.json.converters.PathConverter
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class PathConverter : AttributeConverter<Path, String>, FromJson<Path> by PathConverter, ToJson<Path> by PathConverter {

    override val type = Path::class.java

    override fun convertToDatabaseColumn(attribute: Path?) = attribute?.toString()

    override fun convertToEntityAttribute(dbData: String?) = dbData?.let(Path::of)
}
