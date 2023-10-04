package nebulosa.guiding.internal

enum class GuideDirection {
    UP_NORTH, // DEC+
    DOWN_SOUTH, // DEC-
    LEFT_WEST, // RA+
    RIGHT_EAST; // RA-

    val reversed
        get() = when (this) {
            UP_NORTH -> DOWN_SOUTH
            DOWN_SOUTH -> UP_NORTH
            LEFT_WEST -> RIGHT_EAST
            RIGHT_EAST -> LEFT_WEST
        }
}
