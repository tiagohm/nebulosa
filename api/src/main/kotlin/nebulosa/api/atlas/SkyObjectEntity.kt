package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.atlas.SkyDatabaseWriter.Companion.NAME_SEPARATOR
import nebulosa.math.Angle
import nebulosa.math.Velocity
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.DeepSkyObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.InstantOfTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

data class SkyObjectEntity(
    override var id: Long = 0L,
    override var name: List<String> = emptyList(),
    override var type: SkyObjectType = SkyObjectType.OBJECT_OF_UNKNOWN_NATURE,
    override var rightAscensionJ2000: Angle = 0.0,
    override var declinationJ2000: Angle = 0.0,
    override var magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    override var pmRA: Angle = 0.0,
    override var pmDEC: Angle = 0.0,
    override var parallax: Angle = 0.0,
    override var radialVelocity: Velocity = 0.0,
    override var redshift: Double = 0.0,
    override var constellation: Constellation = Constellation.AND,
) : DeepSkyObject, Body {

    @delegate:Transient private val star by lazy { FixedStar(rightAscensionJ2000, declinationJ2000, pmRA, pmDEC, parallax, radialVelocity) }

    override val center
        @JsonIgnore get() = 0

    override val target
        @JsonIgnore get() = Int.MIN_VALUE

    override fun observedAt(observer: ICRF) = star.observedAt(observer)

    override fun compute(time: InstantOfTime) = star.compute(time)

    fun mapTo(builder: UpdateBuilder<Int>) {
        // builder[SkyObjectTable.id] = id
        builder[SkyObjectTable.name] = name.joinToString(NAME_SEPARATOR)
        builder[SkyObjectTable.type] = type
        builder[SkyObjectTable.rightAscension] = rightAscensionJ2000
        builder[SkyObjectTable.declination] = declinationJ2000
        builder[SkyObjectTable.magnitude] = magnitude
        builder[SkyObjectTable.pmRA] = pmRA
        builder[SkyObjectTable.pmDEC] = pmDEC
        builder[SkyObjectTable.parallax] = parallax
        builder[SkyObjectTable.radialVelocity] = radialVelocity
        builder[SkyObjectTable.redshift] = redshift
        builder[SkyObjectTable.constellation] = constellation
    }

    companion object {

        fun from(row: ResultRow) = SkyObjectEntity(
            row[SkyObjectTable.id],
            row[SkyObjectTable.name].split(NAME_SEPARATOR),
            row[SkyObjectTable.type],
            row[SkyObjectTable.rightAscension],
            row[SkyObjectTable.declination],
            row[SkyObjectTable.magnitude],
            row[SkyObjectTable.pmRA],
            row[SkyObjectTable.pmDEC],
            row[SkyObjectTable.parallax],
            row[SkyObjectTable.radialVelocity],
            row[SkyObjectTable.redshift],
            row[SkyObjectTable.constellation],
        )
    }
}
