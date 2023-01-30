package nebulosa.guiding.local

class GuideStar : Star {

    @JvmField var missCount = 0
    @JvmField var zeroCount = 0
    @JvmField var lostCount = 0

    val referencePoint = ZERO

    constructor(x: Float, y: Float) : super(x, y)

    constructor(point: Point) : super(point)
}
