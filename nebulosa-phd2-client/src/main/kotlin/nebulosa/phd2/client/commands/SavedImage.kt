package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.file.Path

data class SavedImage(@field:JsonProperty("filename") val path: Path)
