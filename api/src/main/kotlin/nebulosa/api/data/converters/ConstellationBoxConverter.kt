package nebulosa.api.data.converters

import io.objectbox.converter.PropertyConverter
import nebulosa.nova.astrometry.Constellation

class ConstellationBoxConverter : PropertyConverter<Constellation?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): Constellation {
        return databaseValue?.let(Constellation::valueOf) ?: Constellation.AND
    }

    override fun convertToDatabaseValue(entityProperty: Constellation?): String {
        return entityProperty?.name ?: "AND"
    }
}
