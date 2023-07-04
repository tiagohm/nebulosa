package nebulosa.api.data.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.data.converters.AutoSubFolderModeConverter
import nebulosa.api.data.enums.AutoSubFolderMode

@Entity
data class CameraPreference(
    @JsonIgnore @Id var id: Long = 0,
    @JsonIgnore @Index var name: String = "",
    var autoSave: Boolean = false,
    var savePath: String = "",
    @Convert(converter = AutoSubFolderModeConverter::class, dbType = String::class)
    var autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.OFF,
)
