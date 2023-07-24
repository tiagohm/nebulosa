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
data class DeepSkyObjectEntity(
    @Id var id: Long = 0L,
    @Index var names: String = "",
    var m: Int = 0,
    var ngc: Int = 0,
    var ic: Int = 0,
    var c: Int = 0,
    var b: Int = 0,
    var sh2: Int = 0,
    var vdb: Int = 0,
    var rcw: Int = 0,
    var ldn: Int = 0,
    var lbn: Int = 0,
    var cr: Int = 0,
    var mel: Int = 0,
    var pgc: Int = 0,
    var ugc: Int = 0,
    var arp: Int = 0,
    var vv: Int = 0,
    var dwb: Int = 0,
    var tr: Int = 0,
    var st: Int = 0,
    var ru: Int = 0,
    var vdbha: Int = 0,
    var ced: String? = null,
    var pk: String? = null,
    var png: String? = null,
    var snrg: String? = null,
    var aco: String? = null,
    var hcg: String? = null,
    var eso: String? = null,
    var vdbh: String? = null,
    var mType: String? = null,
    var magnitude: Double = Double.MAX_VALUE,
    var rightAscension: Double = 0.0,
    var declination: Double = 0.0,
    @Index @Convert(converter = SkyObjectTypeConverter::class, dbType = String::class)
    var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    var redshift: Double = 0.0,
    var parallax: Double = 0.0,
    var radialVelocity: Double = 0.0,
    var distance: Double = 0.0,
    var majorAxis: Double = 0.0,
    var minorAxis: Double = 0.0,
    var orientation: Double = 0.0,
    var pmRA: Double = 0.0,
    var pmDEC: Double = 0.0,
    @Index @Convert(converter = ConstellationConverter::class, dbType = String::class)
    var constellation: Constellation = Constellation.AND,
)
