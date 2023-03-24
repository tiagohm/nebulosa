package nebulosa.guiding.local

class GuideStar : Star {

    @JvmField var missCount = 0
    @JvmField var zeroCount = 0
    @JvmField var lostCount = 0

    @JvmField val offsetFromPrimary = Point()
    @JvmField val referencePoint = Point()

    constructor(x: Double, y: Double) : super(x, y)

    constructor(point: Point) : super(point)
}
