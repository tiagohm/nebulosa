package nebulosa.api.locations

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import jakarta.validation.constraints.NotBlank
import nebulosa.api.database.BoxEntity
import nebulosa.math.deg
import nebulosa.math.m
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import org.hibernate.validator.constraints.Range

@Entity
data class LocationEntity(
    @Id override var id: Long = 0L,
    @field:NotBlank var name: String = "",
    @field:Range(min = -90, max = 90) var latitude: Double = 0.0, // deg.
    @field:Range(min = -180, max = 180) var longitude: Double = 0.0, // deg.
    @field:Range(min = -1000, max = 10000) var elevation: Double = 0.0, // m.
    @field:Range(min = -720, max = 720) var offsetInMinutes: Int = 0,
    var selected: Boolean = false,
) : BoxEntity {

    fun geographicPosition(): GeographicPosition {
        return Geoid.IERS2010.lonLat(longitude.deg, latitude.deg, elevation.m)
    }
}
