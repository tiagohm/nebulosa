package nebulosa.api.locations

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import nebulosa.math.deg
import nebulosa.math.m
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import org.hibernate.validator.constraints.Range

@Entity
@Table(name = "locations")
data class LocationEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @field:NotBlank @Column(name = "name", columnDefinition = "TEXT") var name: String = "",
    @field:Range(min = -90, max = 90) @Column(name = "latitude", columnDefinition = "REAL") var latitude: Double = 0.0, // deg.
    @field:Range(min = -180, max = 180) @Column(name = "longitude", columnDefinition = "REAL") var longitude: Double = 0.0, // deg.
    @field:Range(min = -1000, max = 10000) @Column(name = "elevation", columnDefinition = "REAL") var elevation: Double = 0.0, // m.
    @field:Range(min = -720, max = 720) @Column(name = "offset_in_minutes", columnDefinition = "INT2") var offsetInMinutes: Int = 0,
) {

    fun geographicPosition(): GeographicPosition {
        return Geoid.IERS2010.latLon(longitude.deg, latitude.deg, elevation.m)
    }
}
