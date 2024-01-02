package nebulosa.astrometrynet.plate.solving

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.DoubleByReference
import nebulosa.plate.solving.Parity

@Suppress("FunctionName")
interface LibAstrometryNet : Library {

    // SOLVER

    fun solver_new(): Solver.ByReference

    fun solver_run(solver: Solver.ByReference)

    fun solver_set_keep_logodds(solver: Solver.ByReference, logodds: Double)

    fun solver_set_parity(solver: Solver.ByReference, parity: Parity): Int

    fun solver_did_solve(solver: Solver.ByReference): Boolean

    fun solver_get_field_center(solver: Solver.ByReference, px: DoubleByReference, py: DoubleByReference)

    fun solver_get_field_jitter(solver: Solver.ByReference): Double

    fun solver_get_max_radius_arcsec(solver: Solver.ByReference): Double

    fun solver_get_best_match(solver: Solver.ByReference): Matched.ByReference

    fun solver_get_best_match_index_name(solver: Solver.ByReference): String

    fun solver_get_pixscale_low(solver: Solver.ByReference): Double

    fun solver_get_pixscale_high(solver: Solver.ByReference): Double

    fun solver_set_quad_size_range(solver: Solver.ByReference, min: Double, max: Double)

    fun solver_get_quad_size_range_arcsec(solver: Solver.ByReference, min: Double, max: Double)

    fun solver_set_quad_size_fraction(solver: Solver.ByReference, min: Double, max: Double)

    fun solver_compute_quad_range(solver: Solver.ByReference, index: Index.ByReference, minAB: DoubleByReference, maxAB: DoubleByReference)

    fun solver_reset_counters(solver: Solver.ByReference)

    fun solver_field_width(solver: Solver.ByReference): Double

    fun solver_field_height(solver: Solver.ByReference): Double

    fun solver_set_radec(solver: Solver.ByReference, rightAscension: Double, declination: Double, radius: Double)

    fun solver_clear_radec(solver: Solver.ByReference)

    fun solver_set_field(solver: Solver.ByReference, field: StarXY.ByReference)

    fun solver_set_field_bounds(solver: Solver.ByReference, minX: Double, maxX: Double, minY: Double, maxY: Double)

    fun solver_cleanup_field(solver: Solver.ByReference)

    fun solver_get_field(solver: Solver.ByReference): StarXY.ByReference

    fun solver_free_field(solver: Solver.ByReference)

    fun solver_verify_sip_wcs(solver: Solver.ByReference, sip: Sip.ByReference)

    fun solver_add_index(solver: Solver.ByReference, index: Index.ByReference)

    fun solver_n_indices(solver: Solver.ByReference): Int

    fun solver_get_index(solver: Solver.ByReference, index: Int): Pointer

    fun solver_reset_best_match(solver: Solver.ByReference)

    fun solver_reset_field_size(solver: Solver.ByReference)

    fun solver_preprocess_field(solver: Solver.ByReference)

    fun solver_set_default_values(solver: Solver.ByReference)

    fun solver_clear_indexes(solver: Solver.ByReference)

    fun solver_cleanup(solver: Solver.ByReference)

    fun solver_free(solver: Solver.ByReference)

    // INDEX

    fun index_load(path: String, flags: Int, index: Index.ByReference?): Index.ByReference

    fun index_unload(index: Index.ByReference)

    fun index_reload(index: Index.ByReference): Int

    fun index_dimquads(index: Index.ByReference): Int

    fun index_overlaps_scale_range(index: Index.ByReference, quadLow: Double, quadHigh: Double): Boolean

    fun index_is_within_range(index: Index.ByReference, rightAscension: Double, declination: Double, radius: Double): Boolean

    fun index_is_file_index(path: String): Boolean

    fun index_get_quad_dim(index: Index.ByReference): Int

    fun index_get_code_dim(index: Index.ByReference): Int

    fun index_nquads(index: Index.ByReference): Int

    fun index_nstars(index: Index.ByReference): Int

    fun index_build_from(codekd: Pointer, quads: Pointer, starkd: Pointer): Index

    fun index_close_fds(index: Index.ByReference): Int

    fun index_close(index: Index.ByReference)

    fun index_free(index: Index.ByReference)

    // XY LIST

    fun xylist_open(path: String): XYList.ByReference

    fun xylist_n_fields(xylist: XYList.ByReference): Int

    fun xylist_read_field(xylist: XYList.ByReference, fld: StarXY.ByReference?): StarXY.ByReference

    fun xylist_get_imagew(xylist: XYList.ByReference): Int

    fun xylist_get_imageh(xylist: XYList.ByReference): Int

    fun xylist_close(xylist: XYList.ByReference): Int

    // SIP

    fun sip_create(): Sip.ByReference

    fun sip_free(sip: Sip.ByReference)

    fun sip_pixelxy2radec(sip: Sip.ByReference, px: Double, py: Double, ra: DoubleByReference, dec: DoubleByReference)

    fun sip_pixelxy2xyzarr(sip: Sip.ByReference, px: Double, py: Double, xyz: DoubleByReference)

    fun sip_radec2pixelxy(sip: Sip.ByReference, ra: Double, dec: Double, px: DoubleByReference, py: DoubleByReference): Boolean

    fun sip_xyzarr2pixelxy(sip: Sip.ByReference, xyz: DoubleByReference, px: DoubleByReference, py: DoubleByReference): Boolean

    fun sip_xyz2pixelxy(sip: Sip.ByReference, x: Double, y: Double, z: Double, px: DoubleByReference, py: DoubleByReference): Boolean

    fun tan_det_cd(tan: Tan.ByReference): Double

    fun tan_pixel_scale(tan: Tan.ByReference): Double

    fun sip_det_cd(sip: Sip.ByReference): Double

    fun sip_pixel_scale(sip: Sip.ByReference): Double

    fun tan_pixelxy2radec(tan: Tan.ByReference, px: Double, py: Double, ra: DoubleByReference, dec: DoubleByReference)

    fun tan_pixelxy2xyzarr(tan: Tan.ByReference, px: Double, py: Double, xyz: DoubleByReference)

    fun tan_pixelxy2radecarr(tan: Tan.ByReference, px: Double, py: Double, radec: DoubleByReference)

    fun tan_radec2pixelxy(tan: Tan.ByReference, ra: Double, dec: Double, px: DoubleByReference, py: DoubleByReference): Boolean

    fun tan_xyzarr2pixelxy(tan: Tan.ByReference, ra: Double, xyz: DoubleByReference, px: DoubleByReference, py: DoubleByReference): Boolean

    fun tan_get_orientation(tan: Tan.ByReference): Double

    fun sip_get_orientation(sip: Sip.ByReference): Double

    companion object {

        const val DQMAX = 5
        const val INDEX_ONLY_LOAD_METADATA = 2
        const val NO: Byte = 0
        const val YES: Byte = 1

        const val LIBRARY_NAME = "libastrometry"
        const val PATH = "LIBASTROMETRYNET_PATH"

        @JvmStatic val INSTANCE: LibAstrometryNet by lazy { Native.load(System.getProperty(PATH, LIBRARY_NAME), LibAstrometryNet::class.java) }
    }
}
