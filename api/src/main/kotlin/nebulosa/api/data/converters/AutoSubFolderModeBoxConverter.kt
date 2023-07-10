package nebulosa.api.data.converters

import io.objectbox.converter.PropertyConverter
import nebulosa.api.data.enums.AutoSubFolderMode

class AutoSubFolderModeBoxConverter : PropertyConverter<AutoSubFolderMode?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): AutoSubFolderMode {
        return databaseValue?.let(AutoSubFolderMode::valueOf) ?: AutoSubFolderMode.OFF
    }

    override fun convertToDatabaseValue(entityProperty: AutoSubFolderMode?): String {
        return entityProperty?.name ?: "OFF"
    }
}
