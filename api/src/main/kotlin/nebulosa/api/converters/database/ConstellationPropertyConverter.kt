package nebulosa.api.converters.database

import io.objectbox.converter.PropertyConverter
import nebulosa.nova.astrometry.Constellation

class ConstellationPropertyConverter : PropertyConverter<Constellation?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): Constellation {
        return databaseValue?.let(Constellation.entries::get) ?: Constellation.AND
    }

    override fun convertToDatabaseValue(entityProperty: Constellation?): Int {
        return entityProperty?.ordinal ?: 0
    }
}
