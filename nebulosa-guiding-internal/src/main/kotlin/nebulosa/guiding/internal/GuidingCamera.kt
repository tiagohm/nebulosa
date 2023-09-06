package nebulosa.guiding.internal

interface GuidingCamera {

    val binning: Int

    val pixelScale: Double

    val exposureTime: Long
}
