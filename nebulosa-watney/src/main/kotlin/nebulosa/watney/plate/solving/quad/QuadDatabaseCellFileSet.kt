package nebulosa.watney.plate.solving.quad

/**
 * Class representing a set of Cell files. The quad database is split into cell files.
 */
internal class QuadDatabaseCellFileSet {

    internal data class QuadDatabaseCellFileSet(
        @JvmField val quadsPerSqDeg: Double,
        @JvmField val fileIndex: Int,
        @JvmField val passIndex: Int,
    )
}
