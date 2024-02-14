package nebulosa.api.beans.converters.database

import io.objectbox.converter.PropertyConverter
import nebulosa.indi.device.camera.FrameType

class FrameTypePropertyConverter : PropertyConverter<FrameType?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): FrameType {
        return databaseValue?.let(FrameType.entries::get) ?: FrameType.LIGHT
    }

    override fun convertToDatabaseValue(entityProperty: FrameType?): Int {
        return entityProperty?.ordinal ?: 0
    }
}
