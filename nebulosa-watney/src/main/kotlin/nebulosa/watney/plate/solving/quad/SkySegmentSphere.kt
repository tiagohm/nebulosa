package nebulosa.watney.plate.solving.quad

import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.deg
import kotlin.math.abs

object SkySegmentSphere : Collection<SkySegmentSphere.Cell> {

    data class Cell(val bounds: CoordinateBounds, val bandIndex: Int, val cellIndex: Int) {

        val id = cellId(bandIndex, cellIndex)

        val width
            get() = bounds.declination.let { SphericalCoordinate.angularDistance(bounds.left, it, bounds.right, it) }

        val height: Angle
            get() = abs(bounds.top - bounds.bottom)

        fun subDivide(divisions: Int): Array<CoordinateBounds> {
            val subCellWidth = (bounds.right - bounds.left) / divisions
            val subCellHeight = (bounds.top - bounds.bottom) / divisions

            var n = 0
            val subCells = arrayOfNulls<CoordinateBounds>(divisions * divisions)

            for (decBlock in 1..divisions) {
                for (raBlock in 1..divisions) {
                    subCells[n] = CoordinateBounds(
                        bounds.left + (raBlock - 1) * subCellWidth,
                        bounds.left + raBlock * subCellWidth,
                        bounds.bottom + decBlock * subCellHeight,
                        bounds.bottom + (decBlock - 1) * subCellHeight,
                    )

                    n++
                }
            }

            return subCells.requireNoNulls()
        }

        companion object {

            @JvmStatic
            fun cellId(bandIndex: Int, cellIndex: Int) = "b%02dc%02d".format(bandIndex, cellIndex)
        }
    }

    private val LATITUDE_BANDS = listOf(
        doubleArrayOf(80.1375, 90.0),
        doubleArrayOf(70.2010, 80.1375),
        doubleArrayOf(60.1113, 70.2010),
        doubleArrayOf(50.2170, 60.1113),
        doubleArrayOf(40.5602, 50.2170),
        doubleArrayOf(30.1631, 40.5602),
        doubleArrayOf(20.7738, 30.1631),
        doubleArrayOf(10.2148, 20.7738),
        doubleArrayOf(0.0, 10.2148),
        doubleArrayOf(-10.2148, 0.0),
        doubleArrayOf(-20.7738, -10.2148),
        doubleArrayOf(-30.1631, -20.7738),
        doubleArrayOf(-40.5602, -30.1631),
        doubleArrayOf(-50.2170, -40.5602),
        doubleArrayOf(-60.1113, -50.2170),
        doubleArrayOf(-70.2010, -60.1113),
        doubleArrayOf(-80.1375, -70.2010),
        doubleArrayOf(-90.0, -80.1375),
    )

    private val CELL_WIDTHS =
        intArrayOf(120, 40, 24, 18, 15, 12, 12, 10, 10, 10, 10, 12, 12, 15, 18, 24, 40, 120)

    private val CELLS: Array<Array<Cell?>>

    init {
        val cells = ArrayList<Cell>()

        for (b in LATITUDE_BANDS.indices) {
            val band = LATITUDE_BANDS[b]
            val cellWidth = CELL_WIDTHS[b]
            var raLeft = 0
            var c = 0

            while (raLeft < 360) {
                val bounds = CoordinateBounds(raLeft.deg, (raLeft + cellWidth).deg, band[0].deg, band[1].deg)
                val cell = Cell(bounds, b, c)
                cells.add(cell)
                raLeft += cellWidth
                c++
            }
        }

        CELLS = Array(cells.maxOf { it.bandIndex } + 1) { arrayOfNulls(cells.maxOf { it.cellIndex } + 1) }
        cells.forEach { CELLS[it.bandIndex][it.cellIndex] = it }
    }

    override val size
        get() = CELLS.size

    override fun contains(element: Cell): Boolean {
        return CELLS.any { element in it }
    }

    override fun containsAll(elements: Collection<Cell>): Boolean {
        return elements.all { it in this }
    }

    override fun isEmpty(): Boolean {
        return CELLS.isEmpty()
    }

    override fun iterator(): Iterator<Cell> {
        return CellIterator(CELLS)
    }

    operator fun get(bandIndex: Int, cellIndex: Int) = CELLS[bandIndex][cellIndex]!!

    fun withId(id: String): Cell? {
        for (band in CELLS) {
            for (cell in band) {
                if (cell?.id == id) return cell
            }
        }

        return null
    }

    fun cellAt(location: CoordinateBounds): Cell {
        var latIndex = 0

        for (i in LATITUDE_BANDS.indices) {
            if (location.declination > LATITUDE_BANDS[i][0] && location.declination <= LATITUDE_BANDS[i][1]) {
                latIndex = i
                break
            }
        }

        val cellIndex = (location.rightAscension / CELL_WIDTHS[latIndex]).toInt()
        return CELLS[latIndex][cellIndex]!!
    }

    @Suppress("ArrayInDataClass")
    private data class CellIterator(private val cells: Array<Array<Cell?>>) : Iterator<Cell> {

        @Volatile private var bandIndex = 0
        @Volatile private var cellIndex = 0

        override fun hasNext(): Boolean {
            return bandIndex < cells.size && cellIndex < cells[bandIndex].size
        }

        override fun next(): Cell {
            val cell = cells[bandIndex][cellIndex++]!!

            if (cellIndex >= cells[bandIndex].size) {
                cellIndex = 0
                bandIndex++
            }

            return cell
        }

    }
}
