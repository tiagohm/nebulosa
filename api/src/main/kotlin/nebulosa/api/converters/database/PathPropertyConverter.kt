package nebulosa.api.converters.database

import io.objectbox.converter.PropertyConverter
import java.nio.file.Path

class PathPropertyConverter : PropertyConverter<Path?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): Path? {
        return databaseValue?.let(Path::of)
    }

    override fun convertToDatabaseValue(entityProperty: Path?): String? {
        return entityProperty?.toString()
    }
}
