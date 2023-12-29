package nebulosa.simbad

import nebulosa.adql.SortDirection
import nebulosa.math.Angle
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType

@Suppress("ArrayInDataClass")
data class SimbadSearch(
    internal val id: Long = NO_ID,
    internal val text: String? = null,
    internal val rightAscension: Angle = 0.0,
    internal val declination: Angle = 0.0,
    internal val radius: Angle = 0.0,
    internal val types: List<SkyObjectType>? = null,
    internal val magnitudeMin: Double = SkyObject.MAGNITUDE_MIN,
    internal val magnitudeMax: Double = SkyObject.MAGNITUDE_MAX,
    internal val constellation: Constellation? = null,
    internal val ids: LongArray = LongArray(0),
    internal val lastID: Long = NO_ID,
    internal val limit: Int = SimbadService.DEFAULT_LIMIT,
    internal val sortType: SortType = SortType.OID,
    internal val sortDirection: SortDirection = SortDirection.ASCENDING,
) {

    enum class SortType {
        OID,
        MAGNITUDE,
    }

    private constructor(builder: Builder) : this(
        builder.id, builder.text, builder.rightAscension, builder.declination, builder.radius,
        builder.types, builder.magnitudeMin, builder.magnitudeMax, builder.constellation,
        builder.ids, builder.lastID, builder.limit, builder.sortType, builder.sortDirection,
    )

    class Builder {

        internal var id = NO_ID
        internal var text: String? = null
        internal var rightAscension: Angle = 0.0
        internal var declination: Angle = 0.0
        internal var radius: Angle = 0.0
        internal val types: MutableList<SkyObjectType> = ArrayList()
        internal var magnitudeMin = SkyObject.MAGNITUDE_MIN
        internal var magnitudeMax = SkyObject.MAGNITUDE_MAX
        internal var constellation: Constellation? = null
        internal var ids = LongArray(0)
        internal var lastID = 0L
        internal var limit = SimbadService.DEFAULT_LIMIT
        internal var sortType = SortType.OID
        internal var sortDirection = SortDirection.ASCENDING

        fun id(id: Long) = apply { this.id = id }

        fun text(text: String?) = apply { this.text = text }

        fun region(rightAscension: Angle, declination: Angle, radius: Angle) = apply {
            this.rightAscension = rightAscension
            this.declination = declination
            this.radius = radius
        }

        fun types(vararg types: SkyObjectType) = apply { types.forEach(this.types::add) }

        fun types(types: Iterable<SkyObjectType>) = apply { this.types.addAll(types) }

        fun noMagnitude() = apply { magnitude(SkyObject.MAGNITUDE_RANGE) }

        fun magnitudeMin(magnitudeMin: Double) = apply { this.magnitudeMin = magnitudeMin }

        fun magnitudeMax(magnitudeMax: Double) = apply { this.magnitudeMax = magnitudeMax }

        fun magnitude(range: ClosedFloatingPointRange<Double>) = apply {
            this.magnitudeMin = range.start
            this.magnitudeMax = range.endInclusive
        }

        fun constellation(constellation: Constellation?) = apply { this.constellation = constellation }

        fun limit(limit: Int) = apply { this.limit = limit }

        fun lastID(lastID: Long) = apply { this.lastID = lastID }

        fun ids(ids: LongArray) = apply { this.ids = ids }

        fun sortBy(sortType: SortType, ascending: SortDirection = SortDirection.ASCENDING) = apply {
            this.sortType = sortType
            this.sortDirection = ascending
        }

        fun build() = SimbadSearch(this)
    }

    companion object {

        const val NO_ID = -1L
    }
}
