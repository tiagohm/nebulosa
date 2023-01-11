package nebulosa.guiding

class GuideStar : Star {

    val referencePoint = ZERO
    @JvmField var missCount = 0
    @JvmField var zeroCount = 0
    @JvmField var lostCount = 0

    constructor(x: Double, y: Double) : super(x, y)

    constructor(point: Point) : super(point)
}
