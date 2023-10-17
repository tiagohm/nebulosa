package nebulosa.guiding

enum class GuideDirection {
    NORTH, // UP, DEC+
    SOUTH, // DOWN, DEC-
    WEST, // LEFT, RA+
    EAST; // RIGHT, RA-

    val reversed
        get() = when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            WEST -> EAST
            EAST -> WEST
        }
}
