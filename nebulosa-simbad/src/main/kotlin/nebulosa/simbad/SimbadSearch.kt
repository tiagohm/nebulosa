package nebulosa.simbad

import nebulosa.math.Angle
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType

data class SimbadSearch(
    internal val id: Long = NO_ID,
    internal val text: String? = null,
    internal val rightAscension: Angle = 0.0,
    internal val declination: Angle = 0.0,
    internal val radius: Angle = 0.0,
    internal val types: List<SkyObjectType> = emptyList(),
    internal val magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE,
    internal val magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE,
    internal val limit: Int = 1000,
) {

    private constructor(builder: Builder) : this(
        builder.id, builder.text, builder.rightAscension, builder.declination, builder.radius,
        builder.types, builder.magnitudeMin, builder.magnitudeMax, builder.limit
    )

    class Builder {

        internal var id: Long = NO_ID
        internal var text: String? = null
        internal var rightAscension: Angle = 0.0
        internal var declination: Angle = 0.0
        internal var radius: Angle = 0.0
        internal val types: MutableList<SkyObjectType> = ArrayList()
        internal var magnitudeMin: Double = -SkyObject.UNKNOWN_MAGNITUDE
        internal var magnitudeMax: Double = SkyObject.UNKNOWN_MAGNITUDE
        internal var limit: Int = 1000

        fun id(id: Long) = apply { this.id = id }

        fun text(text: String?) = apply { this.text = text }

        fun region(rightAscension: Angle, declination: Angle, radius: Angle) = apply {
            this.rightAscension = rightAscension
            this.declination = declination
            this.radius = radius
        }

        fun types(vararg types: SkyObjectType) = apply { types.forEach(this.types::add) }

        fun types(types: Iterable<SkyObjectType>) = apply { this.types.addAll(types) }

        fun noMagnitude() = apply { magnitude(NO_MAGNITUDE_RANGE) }

        fun magnitudeMin(magnitudeMin: Double) = apply { this.magnitudeMin = magnitudeMin }

        fun magnitudeMax(magnitudeMax: Double) = apply { this.magnitudeMax = magnitudeMax }

        fun magnitude(range: ClosedFloatingPointRange<Double>) = apply {
            this.magnitudeMin = range.start
            this.magnitudeMax = range.endInclusive
        }

        fun limit(limit: Int) = apply { this.limit = limit }

        fun build() = SimbadSearch(this)
    }

    companion object {

        const val NO_ID = -1L
        @JvmStatic val NO_MAGNITUDE_RANGE = -SkyObject.UNKNOWN_MAGNITUDE..SkyObject.UNKNOWN_MAGNITUDE
    }
}
