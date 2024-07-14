import com.sun.jna.Pointer
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nebulosa.astrometrynet.platesolver.*
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.min

@EnabledIf(NonGitHubOnlyCondition::class)
class LibAstrometryNetTest : StringSpec(), Solver.RecordMatchCallback {

    init {
        System.setProperty("LIBASTROMETRYNET_PATH", "/home/tiagohm/Git/astrometry.net/solver/libastrometry.so")

        val lib = LibAstrometryNet.INSTANCE

        // http://data.astrometry.net/
        val indexDir = Path.of(System.getProperty("user.home"), "Downloads", "Index Files")

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
                val index = lib.index_load("$indexDir/index-41%02d.fits".format(i), 0, null)
                index.indexName shouldBe "$indexDir/index-41%02d.fits".format(i)
                index.indexId shouldBeExactly 4100 + i
                index.cutnsweep shouldBeExactly 10
                lib.index_close(index)
                lib.index_free(index)
            }
        }
        "open xyls" {
            val xyls = lib.xylist_open("/home/tiagohm/Git/astrometry.net/solver/apod4.xy")
            xyls.xname shouldBe "X"
            xyls.yname shouldBe "Y"
            xyls.xtype shouldBeExactly 8
            xyls.ytype shouldBeExactly 8
            xyls.includeFlux shouldBe LibAstrometryNet.YES
            xyls.includeBackground shouldBe LibAstrometryNet.YES
        }
        "new solver" {
            val solver = lib.solver_new()
            solver shouldNotBe Pointer.NULL
            lib.solver_free(solver)
        }
        "run solver" {
            val solver = lib.solver_new()

            // https://github.com/dstndstn/astrometry.net/blob/main/solver/control-program.c

            solver.recordMatchCallback = this@LibAstrometryNetTest
            solver.funitsLower = 0.1
            solver.funitsUpper = 10.0
            solver.distanceFromQuadBonus = 1
            solver.quadSizeMin = 0.1 * min(719, 507) // image width, height
            solver.quadSizeMax = hypot(719.0, 507.0)
            solver.doTweak = 1
            solver.tweakAbOrder = 1
            solver.tweakAbpOrder = 4
            solver.write()

            lib.solver_set_keep_logodds(solver, ln(1e12))

            indexDir.listDirectoryEntries("*.fits").sorted().forEach {
                val index = lib.index_load("$it", 0, null)
                println(it)
                lib.solver_add_index(solver, index)
            }

            val xyls = lib.xylist_open("/home/tiagohm/Git/astrometry.net/solver/apod4.xy")
            val xy = lib.xylist_read_field(xyls, null)
            lib.solver_reset_counters(solver)
            lib.solver_reset_best_match(solver)
            lib.solver_set_field(solver, xy)
            lib.solver_set_field_bounds(solver, 0.0, 719.0, 0.0, 507.0)
            lib.solver_preprocess_field(solver)
            solver.read()
            println(solver)
            lib.solver_run(solver)
            solver.read()
            println(solver)

            lib.solver_did_solve(solver).shouldBeTrue()

            lib.xylist_close(xyls)
            lib.solver_free_field(solver)
            lib.solver_free(solver)
        }
    }

    override fun matchFound(matched: Matched.ByReference, userData: Pointer?): Byte {
        println(matched)
        return LibAstrometryNet.YES
    }
}
