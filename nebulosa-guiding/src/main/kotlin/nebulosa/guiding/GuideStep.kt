package nebulosa.guiding

interface GuideStep {

    val frame: Int

    val time: Double

    val raDistance: Double

    val decDistance: Double

    val raDuration: Long

    val decDuration: Long
}
