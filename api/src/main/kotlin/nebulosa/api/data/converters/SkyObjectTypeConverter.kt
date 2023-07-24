package nebulosa.api.data.converters

import io.objectbox.converter.PropertyConverter
import nebulosa.skycatalog.SkyObjectType

class SkyObjectTypeConverter : PropertyConverter<SkyObjectType?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): SkyObjectType {
        return databaseValue?.let(SkyObjectType::valueOf) ?: SkyObjectType.OBJECT_OF_UNKNOWN_NATURE
    }

    override fun convertToDatabaseValue(entityProperty: SkyObjectType?): String {
        return entityProperty?.name ?: "OBJECT_OF_UNKNOWN_NATURE"
    }
}
