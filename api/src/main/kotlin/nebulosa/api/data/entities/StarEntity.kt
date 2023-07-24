package nebulosa.api.data.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.data.converters.ConstellationConverter
import nebulosa.api.data.converters.SkyObjectTypeConverter
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType

@Entity
data class StarEntity(
    @Id var id: Long = 0L,
    var hd: Int = 0,
    var hr: Int = 0,
    var hip: Int = 0,
    @Index var names: String = "",
    var magnitude: Double = Double.MAX_VALUE,
    var rightAscension: Double = 0.0,
    var declination: Double = 0.0,
    @Index @Convert(converter = SkyObjectTypeConverter::class, dbType = String::class)
    var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    var spType: String? = null,
    var redshift: Double = 0.0,
    var parallax: Double = 0.0,
    var radialVelocity: Double = 0.0,
    var distance: Double = 0.0,
    var pmRA: Double = 0.0,
    var pmDEC: Double = 0.0,
    @Index @Convert(converter = ConstellationConverter::class, dbType = String::class)
    var constellation: Constellation = Constellation.AND,
)
