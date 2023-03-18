import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import nebulosa.math.Vector3D
import nebulosa.skycatalog.GeodesicGrid

@Ignored
class GeodesicGridTest : StringSpec(), GeodesicGrid.Traverser {

    init {
        "visit" {
            val grid = GeodesicGrid(2)
            grid.visitTriangles(2, this@GeodesicGridTest)
        }
    }

    override fun traverse(level: Int, index: Int, c0: Vector3D, c1: Vector3D, c2: Vector3D) {
        println("$level/$index. c0=$c0, c1=$c1, c2=$c2")
    }
}
