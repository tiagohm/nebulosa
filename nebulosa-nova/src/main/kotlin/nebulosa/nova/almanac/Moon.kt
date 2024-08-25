package nebulosa.nova.almanac

import nebulosa.time.InstantOfTime

const val SYNODIC_MONTH = 29.530588861

fun InstantOfTime.lunation(): Int {
    return ((value - 2423436.6115277777) / SYNODIC_MONTH).toInt() + 1
}
