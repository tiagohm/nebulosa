package nebulosa.time

sealed interface Timescale {

    val ut1: UT1

    val utc: UTC

    val tai: TAI

    val tt: TT

    val tcg: TCG

    val tdb: TDB

    val tcb: TCB
}
