package nebulosa.api.data.converters

import io.objectbox.converter.PropertyConverter
import nebulosa.guiding.GuideParity

class GuideParityConverter : PropertyConverter<GuideParity?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): GuideParity {
        return databaseValue?.let(GuideParity::valueOf) ?: GuideParity.UNKNOWN
    }

    override fun convertToDatabaseValue(entityProperty: GuideParity?): String {
        return entityProperty?.name ?: "UNKNOWN"
    }
}
