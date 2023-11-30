import com.sun.jna.Pointer
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nebulosa.astrometrynet.plate.solving.*
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

@EnabledIf(NonGitHubOnlyCondition::class)
class LibAstrometryNetTest : StringSpec(), Solver.RecordMatchCallback, LibAstrometryNet by LibAstrometryNet.INSTANCE {

    init {
        // http://data.astrometry.net/
        val indexDir = Path.of("/home/tiagohm/Downloads/Index Files")

        "structure sizes" {
            val tan = Tan.ByValue()
            tan.size() shouldBeExactly 88

            val sip = Sip.ByValue()
            sip.size() shouldBeExactly 3304

            val index = Index.ByValue()
            index.size() shouldBeExactly 136

            val xylist = XYList.ByValue()
            xylist.size() shouldBeExactly 72

            val starxy = StarXY.ByValue()
            starxy.size() shouldBeExactly 72

            val matched = Matched.ByValue()
            matched.size() shouldBeExactly 752

            val solver = Solver.ByValue()
            solver.size() shouldBeExactly 1208
        }
        "load index" {
            for (i in 7..19) {
                val index = index_load("$indexDir/index-41%02d.fits".format(i), 0, null)
                index.indexName shouldBe "$indexDir/index-41%02d.fits".format(i)
                index.indexId shouldBeExactly 4100 + i
                index.cutnsweep shouldBeExactly 10
                index_close(index)
                index_free(index)
            }
        }
        "open xyls" {
            val xyls = xylist_open("/home/tiagohm/Git/astrometry.net/dist/examples/apod4.xyls")
            xyls.xname shouldBe "X"
            xyls.yname shouldBe "Y"
            xyls.xtype shouldBeExactly 8
            xyls.ytype shouldBeExactly 8
            xyls.includeFlux shouldBe LibAstrometryNet.YES
            xyls.includeBackground shouldBe LibAstrometryNet.YES
        }
        "new solver" {
            val solver = solver_new()
            solver shouldNotBe Pointer.NULL
            solver_free(solver)
        }
        "run solver" {
            val solver = solver_new()

            solver.recordMatchCallback = this@LibAstrometryNetTest
            solver.quadSizeMin = 0.1
            solver.quadSizeMax = 1.0

            indexDir.listDirectoryEntries("*.fits").forEach {
                val index = index_load("$it", 0, null)
                solver_add_index(solver, index)
            }

            println(solver_n_indices(solver))

            val xyls = xylist_open("/home/tiagohm/Git/astrometry.net/dist/examples/apod4.xyls")
            val xy = xylist_read_field(xyls, null)
            solver_set_field(solver, xy)
            solver_set_field_bounds(solver, 0.0, 719.0, 0.0, 507.0)
            solver.read()
            println(solver)
            solver_run(solver)
            println(solver)

            solver_did_solve(solver).shouldBeTrue()

            xylist_close(xyls)
            solver_free_field(solver)
            solver_free(solver)
        }
    }

    override fun matchFound(matched: Matched.ByReference, userData: Pointer?): Byte {
        println(matched)
        return LibAstrometryNet.YES
    }
}
