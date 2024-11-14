import com.sun.jna.Pointer
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nebulosa.astrometrynet.platesolver.Index
import nebulosa.astrometrynet.platesolver.LibAstrometryNet
import nebulosa.astrometrynet.platesolver.Matched
import nebulosa.astrometrynet.platesolver.Sip
import nebulosa.astrometrynet.platesolver.Solver
import nebulosa.astrometrynet.platesolver.StarXY
import nebulosa.astrometrynet.platesolver.Tan
import nebulosa.astrometrynet.platesolver.XYList
import nebulosa.test.NonGitHubOnly
import nebulosa.test.homeDirectory
import org.junit.jupiter.api.Test
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.min

// git clone --depth=1 https://github.com/dstndstn/astrometry.net.git
// cd astrometry.net
// sudo apt install libcairo2-dev libnetpbm10-dev netpbm libpng-dev libjpeg-dev zlib1g-dev libbz2-dev libcfitsio-dev wcslib-dev
// make
// Use the generated shared library at "solver/libastrometry.so"

@NonGitHubOnly
class LibAstrometryNetTest : Solver.RecordMatchCallback {

    init {
        System.setProperty(LibAstrometryNet.PATH, "$homeDirectory/Git/astrometry.net/solver/libastrometry.so")
    }

    @Test
    fun structureSizes() {
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

    @Test
    fun loadIndex() {
        val index = LibAstrometryNet.INSTANCE.index_load("/usr/local/astrometry/data/index-4119.fits", 0, null)
        index.indexName shouldBe "/usr/local/astrometry/data/index-4119.fits"
        index.indexId shouldBeExactly 4119
        index.cutnsweep shouldBeExactly 10
        LibAstrometryNet.INSTANCE.index_close(index)
        LibAstrometryNet.INSTANCE.index_free(index)
    }

    @Test
    fun openXyls() {
        val xyls = LibAstrometryNet.INSTANCE.xylist_open("$homeDirectory/Git/astrometry.net/solver/apod4.xy")
        xyls.xname shouldBe "X"
        xyls.yname shouldBe "Y"
        xyls.xtype shouldBeExactly 8
        xyls.ytype shouldBeExactly 8
        xyls.includeFlux shouldBe LibAstrometryNet.YES
        xyls.includeBackground shouldBe LibAstrometryNet.YES
    }

    @Test
    fun newSolver() {
        val solver = LibAstrometryNet.INSTANCE.solver_new()
        solver shouldNotBe Pointer.NULL
        LibAstrometryNet.INSTANCE.solver_free(solver)
    }

    @Test
    fun runSolver() {
        val solver = LibAstrometryNet.INSTANCE.solver_new()

        // https://github.com/dstndstn/astrometry.net/blob/main/solver/control-program.c

        val imageW = 900.0
        val imageH = 675.0

        solver.recordMatchCallback = this@LibAstrometryNetTest
        solver.funitsLower = (DEFAULT_ARCMIN_MIN / imageW) * 60.0 // arcmin -> arcsec
        solver.funitsUpper = (DEFAULT_ARCMIN_MAX / imageW) * 60.0 // arcmin -> arcsec
        solver.distanceFromQuadBonus = 1
        solver.quadSizeMin = QSF_MIN * min(imageW, imageH)
        solver.quadSizeMax = hypot(imageW, imageH)
        solver.doTweak = 1
        solver.tweakAbOrder = 1
        solver.tweakAbpOrder = 4
        solver.write()

        LibAstrometryNet.INSTANCE.solver_set_keep_logodds(solver, ln(1e12))

        val index = LibAstrometryNet.INSTANCE.index_load("/usr/local/astrometry/data/index-4119.fits", 0, null)
        LibAstrometryNet.INSTANCE.solver_add_index(solver, index)

        val xyls = LibAstrometryNet.INSTANCE.xylist_open("$homeDirectory/Git/astrometry.net/demo/apod5.xyls")
        val xy = LibAstrometryNet.INSTANCE.xylist_read_field(xyls, null)
        LibAstrometryNet.INSTANCE.solver_reset_counters(solver)
        LibAstrometryNet.INSTANCE.solver_reset_best_match(solver)
        LibAstrometryNet.INSTANCE.solver_set_field(solver, xy)
        LibAstrometryNet.INSTANCE.solver_set_field_bounds(solver, 0.0, imageW, 0.0, imageH)
        LibAstrometryNet.INSTANCE.solver_preprocess_field(solver)
        solver.read()

        LibAstrometryNet.INSTANCE.solver_run(solver)
        solver.read()

        try {
            LibAstrometryNet.INSTANCE.solver_did_solve(solver).shouldBeTrue()
        } finally {
            LibAstrometryNet.INSTANCE.xylist_close(xyls)
            LibAstrometryNet.INSTANCE.solver_free_field(solver)
            LibAstrometryNet.INSTANCE.solver_free(solver)
        }
    }

    override fun matchFound(matched: Matched.ByReference, userData: Pointer?): Byte {
        println(matched)
        return LibAstrometryNet.YES
    }

    companion object {

        private const val QSF_MIN = 0.1
        private const val DEFAULT_IMAGEW = 1024
        private const val DEFAULT_IMAGEH = 1024
        private const val DEFAULT_ARCMIN_MIN = 15.0
        private const val DEFAULT_ARCMIN_MAX = 25.0
    }
}
