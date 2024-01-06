package nebulosa.api.beans.converters

import io.objectbox.converter.PropertyConverter
import nebulosa.skycatalog.SkyObjectType

class SkyObjectTypePropertyConverter : PropertyConverter<SkyObjectType?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): SkyObjectType {
        return databaseValue?.let(SkyObjectType.entries::get) ?: SkyObjectType.OBJECT_OF_UNKNOWN_NATURE
    }

    override fun convertToDatabaseValue(entityProperty: SkyObjectType?): Int {
        return entityProperty?.ordinal ?: 0
    }
}
