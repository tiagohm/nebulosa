package nebulosa.guiding.internal

class GuideStar : Star {

    var missCount = 0
        internal set

    var zeroCount = 0
        internal set

    var wasLost = false
        internal set

    val offsetFromPrimary = Point()
    val referencePoint = Point()

    constructor(x: Double, y: Double) : super(x, y)

    constructor(point: Point) : super(point)
}
