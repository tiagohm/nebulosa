package nebulosa.wcs

import com.sun.jna.*
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.ptr.DoubleByReference
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import oshi.PlatformEnum
import oshi.SystemInfo
import java.nio.DoubleBuffer
import java.nio.IntBuffer
import java.nio.file.Files
import kotlin.io.path.outputStream

interface LibWCS : Library {

    /**
     * Native method to transforms pixel coordinates to world coordinates
     * using the WCSLIBwcsp2s() C method.
     *
     * @return native method status value, 0 indicates success, other values
     * indicate a problem during method exection. The STATUS_ERRORS
     * array maps the status value to an error message.
     */
    fun wcsp2s(
        wcs: Wcsprm,
        ncoord: Int,
        nelem: Int,
        pixcrd: DoubleArray?,
        imgcrd: DoubleBuffer?,
        phi: DoubleBuffer?,
        theta: DoubleBuffer?,
        world: DoubleBuffer?,
        stat: IntBuffer?,
    ): Int

    /**
     * Native method to transforms world coordinates to pixel coordinates
     * using the WCSLIB wcss2p() C method.
     *
     * @return native method status value, 0 indicates success, other values
     * indicate a problem during method exection. The STATUS_ERRORS
     * array maps the status value to an error message.
     */
    fun wcss2p(
        wcs: Wcsprm?,
        ncoord: Int,
        nelem: Int,
        world: DoubleArray?,
        phi: DoubleBuffer?,
        theta: DoubleBuffer?,
        imgcrd: DoubleBuffer?,
        pixcrd: DoubleBuffer?,
        stat: IntBuffer?,
    ): Int

    fun wcsinit(alloc: Boolean, naxis: Int, wcs: Wcsprm, npvmax: Int, npsmax: Int, ndpmax: Int): Int

    @FieldOrder("i", "m", "value")
    open class PvCard(
        @JvmField var i: Int = 0,
        @JvmField var m: Int = 0,
        @JvmField var value: Double = 0.0,
    ) : Structure() {

        open class ByReference : PvCard(), Structure.ByReference

        open class ByValue : PvCard(), Structure.ByValue
    }

    @FieldOrder("i", "m", "value")
    open class PsCard(
        @JvmField var i: Int = 0,
        @JvmField var m: Int = 0,
        @JvmField var value: ByteArray = ByteArray(72),
    ) : Structure() {

        open class ByReference : PsCard(), Structure.ByReference

        open class ByValue : PsCard(), Structure.ByValue
    }

    @FieldOrder(
        "rsunref", "dsunobs", "crlnobs", "hglnobs", "hgltobs", "aradius",
        "bradius", "cradius", "blonobs", "blatobs", "bdisobs", "dummy"
    )
    open class Auxprm(
        @JvmField var rsunref: Double = 0.0,
        @JvmField var dsunobs: Double = 0.0,
        @JvmField var crlnobs: Double = 0.0,
        @JvmField var hglnobs: Double = 0.0,
        @JvmField var hgltobs: Double = 0.0,
        @JvmField var aradius: Double = 0.0,
        @JvmField var bradius: Double = 0.0,
        @JvmField var cradius: Double = 0.0,
        @JvmField var blonobs: Double = 0.0,
        @JvmField var blatobs: Double = 0.0,
        @JvmField var bdisobs: Double = 0.0,
        @JvmField var dummy: DoubleArray = DoubleArray(2),
    ) : Structure() {

        open class ByReference : Auxprm(), Structure.ByReference

        open class ByValue : Auxprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "m", "k", "map", "crval",
        "index", "coord", "nc", "padding",
        "sense", "p0", "delta", "extrema",
        "err", "mflag", "mm", "mn", "setm", "mk", "mmap", "mcrval",
        "mindex", "mindxs", "mcoord"
    )
    open class Tabprm(
        @JvmField var flag: Int = 0,
        @JvmField var m: Int = 0,
        @JvmField var k: IntByReference? = null,
        @JvmField var map: IntByReference? = null,
        @JvmField var crval: DoubleByReference? = null,
        @JvmField var index: PointerByReference? = null,
        @JvmField var coord: DoubleByReference? = null,
        @JvmField var nc: Int = 0,
        @JvmField var padding: Int = 0,
        @JvmField var sense: IntByReference? = null,
        @JvmField var p0: IntByReference? = null,
        @JvmField var delta: DoubleByReference? = null,
        @JvmField var extrema: DoubleByReference? = null,
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var mflag: Int = 0,
        @JvmField var mm: Int = 0,
        @JvmField var mn: Int = 0,
        @JvmField var setm: Int = 0,
        @JvmField var mk: IntByReference? = null,
        @JvmField var mmap: IntByReference? = null,
        @JvmField var mcrval: DoubleByReference? = null,
        @JvmField var mindex: PointerByReference? = null,
        @JvmField var mindxs: PointerByReference? = null,
        @JvmField var mcoord: DoubleByReference? = null,
    ) : Structure() {

        open class ByReference : Tabprm(), Structure.ByReference

        open class ByValue : Tabprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "naxis", "crpix", "pc", "cdelt", "crval", "cunit", "ctype",
        "lonpole", "latpole", "restfrq", "restwav", "npv", "npvmax", "pv", "nps",
        "npsmax", "ps", "cd", "crota", "altlin", "velref", "alt", "colnum",
        "colax", "cname", "crder", "csyer", "czphs", "cperi", "wcsname", "timesys",
        "trefpos", "trefdir", "plephem", "timeunit", "dateref", "mjdref", "timeoffs", "dateobs",
        "datebeg", "dateavg", "dateend", "mjdobs", "mjdbeg", "mjdavg", "mjdend", "jepoch",
        "bepoch", "tstart", "tstop", "xposure", "telapse", "timsyer", "timrder", "timedel",
        "timepixr", "obsgeo", "obsorbit", "radesys", "equinox", "specsys", "ssysobs", "velosys",
        "zsource", "ssyssrc", "velangl", "aux", "ntab", "nwtb", "tab", "wtb",
        "lngtyp", "lattyp", "lng", "lat", "spec", "time", "cubeface", "dummy",
        "types", "lin", "cel", "spc", "err", "mflag", "mnaxis", "mcrpix",
        "mpc", "mcdelt", "mcrval", "mcunit", "mctype", "mpv", "mps", "mcd",
        "mcrota", "mcolax", "mcname", "mcrder", "mcsyer", "mczphs", "mcperi", "maux",
        "mtab", "mwtb"
    )
    open class Wcsprm(
        @JvmField var flag: Int = 0,
        @JvmField var naxis: Int = 0,
        @JvmField var crpix: DoubleByReference? = null,
        @JvmField var pc: DoubleByReference? = null,
        @JvmField var cdelt: DoubleByReference? = null,
        @JvmField var crval: DoubleByReference? = null,
        @JvmField var cunit: Pointer? = null,
        @JvmField var ctype: Pointer? = null,
        @JvmField var lonpole: Double = 0.0,
        @JvmField var latpole: Double = 0.0,
        @JvmField var restfrq: Double = 0.0,
        @JvmField var restwav: Double = 0.0,
        @JvmField var npv: Int = 0,
        @JvmField var npvmax: Int = 0,
        @JvmField var pv: PvCard.ByReference? = null,
        @JvmField var nps: Int = 0,
        @JvmField var npsmax: Int = 0,
        @JvmField var ps: PsCard.ByReference? = null,
        @JvmField var cd: DoubleByReference? = null,
        @JvmField var crota: DoubleByReference? = null,
        @JvmField var altlin: Int = 0,
        @JvmField var velref: Int = 0,
        @JvmField var alt: ByteArray = ByteArray(4),
        @JvmField var colnum: Int = 0,
        @JvmField var colax: IntByReference? = null,
        @JvmField var cname: Pointer? = null,
        @JvmField var crder: DoubleByReference? = null,
        @JvmField var csyer: DoubleByReference? = null,
        @JvmField var czphs: DoubleByReference? = null,
        @JvmField var cperi: DoubleByReference? = null,
        @JvmField var wcsname: ByteArray = ByteArray(72),
        @JvmField var timesys: ByteArray = ByteArray(72),
        @JvmField var trefpos: ByteArray = ByteArray(72),
        @JvmField var trefdir: ByteArray = ByteArray(72),
        @JvmField var plephem: ByteArray = ByteArray(72),
        @JvmField var timeunit: ByteArray = ByteArray(72),
        @JvmField var dateref: ByteArray = ByteArray(72),
        @JvmField var mjdref: DoubleArray = DoubleArray(2),
        @JvmField var timeoffs: Double = 0.0,
        @JvmField var dateobs: ByteArray = ByteArray(72),
        @JvmField var datebeg: ByteArray = ByteArray(72),
        @JvmField var dateavg: ByteArray = ByteArray(72),
        @JvmField var dateend: ByteArray = ByteArray(72),
        @JvmField var mjdobs: Double = 0.0,
        @JvmField var mjdbeg: Double = 0.0,
        @JvmField var mjdavg: Double = 0.0,
        @JvmField var mjdend: Double = 0.0,
        @JvmField var jepoch: Double = 0.0,
        @JvmField var bepoch: Double = 0.0,
        @JvmField var tstart: Double = 0.0,
        @JvmField var tstop: Double = 0.0,
        @JvmField var xposure: Double = 0.0,
        @JvmField var telapse: Double = 0.0,
        @JvmField var timsyer: Double = 0.0,
        @JvmField var timrder: Double = 0.0,
        @JvmField var timedel: Double = 0.0,
        @JvmField var timepixr: Double = 0.0,
        @JvmField var obsgeo: DoubleArray = DoubleArray(6),
        @JvmField var obsorbit: ByteArray = ByteArray(72),
        @JvmField var radesys: ByteArray = ByteArray(72),
        @JvmField var equinox: Double = 0.0,
        @JvmField var specsys: ByteArray = ByteArray(72),
        @JvmField var ssysobs: ByteArray = ByteArray(72),
        @JvmField var velosys: Double = 0.0,
        @JvmField var zsource: Double = 0.0,
        @JvmField var ssyssrc: ByteArray = ByteArray(72),
        @JvmField var velangl: Double = 0.0,
        @JvmField var aux: Auxprm.ByReference? = null,
        @JvmField var ntab: Int = 0,
        @JvmField var nwtb: Int = 0,
        @JvmField var tab: Tabprm.ByReference? = null,
        @JvmField var wtb: Wtbarr.ByReference? = null,
        @JvmField var lngtyp: ByteArray = ByteArray(8),
        @JvmField var lattyp: ByteArray = ByteArray(8),
        @JvmField var lng: Int = 0,
        @JvmField var lat: Int = 0,
        @JvmField var spec: Int = 0,
        @JvmField var time: Int = 0,
        @JvmField var cubeface: Int = 0,
        @JvmField var dummy: Int = 0,
        @JvmField var types: IntByReference? = null,
        @JvmField var lin: Linprm = Linprm(),
        @JvmField var cel: Celprm = Celprm(),
        @JvmField var spc: Spcprm = Spcprm(),
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var mflag: Int = 0,
        @JvmField var mnaxis: Int = 0,
        @JvmField var mcrpix: DoubleByReference? = null,
        @JvmField var mpc: DoubleByReference? = null,
        @JvmField var mcdelt: DoubleByReference? = null,
        @JvmField var mcrval: DoubleByReference? = null,
        @JvmField var mcunit: Pointer? = null,
        @JvmField var mctype: Pointer? = null,
        @JvmField var mpv: PvCard.ByReference? = null,
        @JvmField var mps: PsCard.ByReference? = null,
        @JvmField var mcd: DoubleByReference? = null,
        @JvmField var mcrota: DoubleByReference? = null,
        @JvmField var mcolax: IntByReference? = null,
        @JvmField var mcname: Pointer? = null,
        @JvmField var mcrder: DoubleByReference? = null,
        @JvmField var mcsyer: DoubleByReference? = null,
        @JvmField var mczphs: DoubleByReference? = null,
        @JvmField var mcperi: DoubleByReference? = null,
        @JvmField var maux: Auxprm.ByReference? = null,
        @JvmField var mtab: Tabprm.ByReference? = null,
        @JvmField var mwtb: Wtbarr.ByReference? = null,
    ) : Structure() {

        class ByReference : Wcsprm(), Structure.ByReference

        class ByValue : Wcsprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "naxis", "crpix", "pc", "cdelt", "dispre", "disseq", "piximg",
        "imgpix", "inaxis", "unity", "affine", "simple", "err", "tmpcrd", "mflag",
        "mnaxis", "mcrpix", "mpc", "mcdelt", "mdispre", "mdisseq"
    )
    open class Linprm(
        @JvmField var flag: Int = 0,
        @JvmField var naxis: Int = 0,
        @JvmField var crpix: DoubleByReference? = null,
        @JvmField var pc: DoubleByReference? = null,
        @JvmField var cdelt: DoubleByReference? = null,
        @JvmField var dispre: Disprm.ByReference? = null,
        @JvmField var disseq: Disprm.ByReference? = null,
        @JvmField var piximg: DoubleByReference? = null,
        @JvmField var imgpix: DoubleByReference? = null,
        @JvmField var inaxis: Int = 0,
        @JvmField var unity: Int = 0,
        @JvmField var affine: Int = 0,
        @JvmField var simple: Int = 0,
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var tmpcrd: DoubleByReference? = null,
        @JvmField var mflag: Int = 0,
        @JvmField var mnaxis: Int = 0,
        @JvmField var mcrpix: DoubleByReference? = null,
        @JvmField var mpc: DoubleByReference? = null,
        @JvmField var mcdelt: DoubleByReference? = null,
        @JvmField var mdispre: Disprm.ByReference? = null,
        @JvmField var mdisseq: Disprm.ByReference? = null,
    ) : Structure() {

        open class ByReference : Linprm(), Structure.ByReference

        open class ByValue : Linprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "naxis", "dtype", "ndp", "ndpmax", "dp", "totdis", "maxdis",
        "docorr", "nhat", "axmap", "offset", "scale", "iparm", "dparm", "inaxis",
        "ndis", "err", "disp2x", "disx2p", "mflag", "mnaxis", "mdtype", "mdp", "mmaxdis"
    )
    open class Disprm(
        @JvmField var flag: Int = 0,
        @JvmField var naxis: Int = 0,
        @JvmField var dtype: Pointer? = null,
        @JvmField var ndp: Int = 0,
        @JvmField var ndpmax: Int = 0,
        @JvmField var dp: Dpkey.ByReference? = null,
        @JvmField var totdis: Double = 0.0,
        @JvmField var maxdis: DoubleByReference? = null,
        @JvmField var docorr: IntByReference? = null,
        @JvmField var nhat: IntByReference? = null,
        @JvmField var axmap: PointerByReference? = null,
        @JvmField var offset: PointerByReference? = null,
        @JvmField var scale: PointerByReference? = null,
        @JvmField var iparm: PointerByReference? = null,
        @JvmField var dparm: PointerByReference? = null,
        @JvmField var inaxis: Int = 0,
        @JvmField var ndis: Int = 0,
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var disp2x: PointerByReference? = null,
        @JvmField var disx2p: PointerByReference? = null,
        @JvmField var mflag: Int = 0,
        @JvmField var mnaxis: Int = 0,
        @JvmField var mdtype: Pointer? = null,
        @JvmField var mdp: Dpkey.ByReference? = null,
        @JvmField var mmaxdis: DoubleByReference? = null,
    ) : Structure() {

        interface Disp2xCallback : Callback {

            fun apply(
                inverse: Int,
                iparm: IntByReference?,
                dparm: DoubleByReference?,
                ncrd: Int,
                rawcrd: DoubleByReference?,
                discrd: DoubleByReference?,
            ): Int
        }

        interface Disx2pCallback : Callback {

            fun apply(
                inverse: Int,
                iparm: IntByReference?,
                dparm: DoubleByReference?,
                ncrd: Int,
                discrd: DoubleByReference?,
                rawcrd: DoubleByReference?,
            ): Int
        }

        open class ByReference : Disprm(), Structure.ByReference

        open class ByValue : Disprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "code", "r0", "pv", "phi0", "theta0", "bounds", "name",
        "category", "pvrange", "simplezen", "equiareal", "conformal", "global", "divergent", "x0",
        "y0", "err", "padding", "w", "m", "n", "prjx2s", "prjs2x"
    )
    open class Prjprm : Structure() {
        @JvmField var flag = 0
        @JvmField var code = ByteArray(4)
        @JvmField var r0 = 0.0
        @JvmField var pv = DoubleArray(30)
        @JvmField var phi0 = 0.0
        @JvmField var theta0 = 0.0
        @JvmField var bounds = 0
        @JvmField var name = ByteArray(40)
        @JvmField var category = 0
        @JvmField var pvrange = 0
        @JvmField var simplezen = 0
        @JvmField var equiareal = 0
        @JvmField var conformal = 0
        @JvmField var global = 0
        @JvmField var divergent = 0
        @JvmField var x0 = 0.0
        @JvmField var y0 = 0.0
        @JvmField var err: Wcserr.ByReference? = null
        @JvmField var padding: Pointer? = null
        @JvmField var w = DoubleArray(10)
        @JvmField var m = 0
        @JvmField var n = 0
        @JvmField var prjx2s: Prjx2sCallback? = null
        @JvmField var prjs2x: Prjs2xCallback? = null

        interface Prjx2sCallback : Callback {

            fun apply(
                prj: Prjprm?,
                nx: Int,
                ny: Int,
                sxy: Int,
                spt: Int,
                x: DoubleByReference?,
                y: DoubleByReference?,
                phi: DoubleByReference?,
                theta: DoubleByReference?,
                stat: IntByReference?,
            ): Int
        }

        interface Prjs2xCallback : Callback {

            fun apply(
                prj: Prjprm?,
                nx: Int,
                ny: Int,
                sxy: Int,
                spt: Int,
                phi: DoubleByReference?,
                theta: DoubleByReference?,
                x: DoubleByReference?,
                y: DoubleByReference?,
                stat: IntByReference?,
            ): Int
        }

        open class ByReference : Prjprm(), Structure.ByReference

        open class ByValue : Prjprm(), Structure.ByValue
    }

    @FieldOrder("flag", "offset", "phi0", "theta0", "ref", "prj", "euler", "latpreq", "isolat", "err", "padding")
    open class Celprm(
        @JvmField var flag: Int = 0,
        @JvmField var offset: Int = 0,
        @JvmField var phi0: Double = 0.0,
        @JvmField var theta0: Double = 0.0,
        @JvmField var ref: DoubleArray = DoubleArray(4),
        @JvmField var prj: Prjprm = Prjprm(),
        @JvmField var euler: DoubleArray = DoubleArray(5),
        @JvmField var latpreq: Int = 0,
        @JvmField var isolat: Int = 0,
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var padding: Pointer? = null,
    ) : Structure() {

        open class ByReference : Celprm(), Structure.ByReference

        open class ByValue : Celprm(), Structure.ByValue
    }

    @FieldOrder(
        "flag", "type", "code", "crval", "restfrq", "restwav", "pv", "w",
        "isGrism", "padding1", "err", "padding2", "spxX2P", "spxP2S", "spxS2P", "spxP2X"
    )
    open class Spcprm(
        @JvmField var flag: Int = 0,
        @JvmField var type: ByteArray = ByteArray(8),
        @JvmField var code: ByteArray = ByteArray(4),
        @JvmField var crval: Double = 0.0,
        @JvmField var restfrq: Double = 0.0,
        @JvmField var restwav: Double = 0.0,
        @JvmField var pv: DoubleArray = DoubleArray(7),
        @JvmField var w: DoubleArray = DoubleArray(6),
        @JvmField var isGrism: Int = 0,
        @JvmField var padding1: Int = 0,
        @JvmField var err: Wcserr.ByReference? = null,
        @JvmField var padding2: Pointer? = null,
        @JvmField var spxX2P: SpxX2PCallback? = null,
        @JvmField var spxP2S: SpxP2SCallback? = null,
        @JvmField var spxS2P: SpxS2PCallback? = null,
        @JvmField var spxP2X: SpxP2XCallback? = null,
    ) : Structure() {

        interface SpxX2PCallback : Callback {

            fun apply(
                param: Double,
                nspec: Int,
                instep: Int,
                outstep: Int,
                inspec: DoubleByReference?,
                outspec: DoubleByReference?,
                stat: IntByReference?,
            ): Int
        }

        interface SpxP2SCallback : Callback {

            fun apply(
                param: Double,
                nspec: Int,
                instep: Int,
                outstep: Int,
                inspec: DoubleByReference?,
                outspec: DoubleByReference?,
                stat: IntByReference?
            ): Int
        }

        interface SpxS2PCallback : Callback {

            fun apply(
                param: Double,
                nspec: Int,
                instep: Int,
                outstep: Int,
                inspec: DoubleByReference?,
                outspec: DoubleByReference?,
                stat: IntByReference?,
            ): Int
        }

        interface SpxP2XCallback : Callback {

            fun apply(
                param: Double,
                nspec: Int,
                instep: Int,
                outstep: Int,
                inspec: DoubleByReference?,
                outspec: DoubleByReference?,
                stat: IntByReference?,
            ): Int
        }

        open class ByReference : Spcprm(), Structure.ByReference

        open class ByValue : Spcprm(), Structure.ByValue
    }

    @FieldOrder("i", "m", "kind", "extnam", "extver", "extlev", "ttype", "row", "ndim", "dimlen", "arrayp")
    open class Wtbarr(
        @JvmField var i: Int = 0,
        @JvmField var m: Int = 0,
        @JvmField var kind: Int = 0,
        @JvmField var extnam: ByteArray = ByteArray(72),
        @JvmField var extver: Int = 0,
        @JvmField var extlev: Int = 0,
        @JvmField var ttype: ByteArray = ByteArray(72),
        @JvmField var row: NativeLong? = null,
        @JvmField var ndim: Int = 0,
        @JvmField var dimlen: IntByReference? = null,
        @JvmField var arrayp: PointerByReference? = null,
    ) : Structure() {

        open class ByReference : Wtbarr(), Structure.ByReference

        open class ByValue : Wtbarr(), Structure.ByValue
    }

    @FieldOrder("field", "j", "type", "value")
    open class Dpkey(
        @JvmField var field: ByteArray = ByteArray(72),
        @JvmField var j: Int = 0,
        @JvmField var type: Int = 0,
        @JvmField var value: Value? = null,
    ) : Structure() {

        open class Value internal constructor(
            var i: Int = 0,
            var f: Double = 0.0,
        ) : Union() {

            companion object {

                fun int(i: Int) = Value(i).also { it.setType(Integer.TYPE) }

                fun double(f: Double) = Value(f = f).also { it.setType(java.lang.Double.TYPE) }
            }
        }

        open class ByReference : Dpkey(), Structure.ByReference

        open class ByValue : Dpkey(), Structure.ByValue
    }

    @FieldOrder("status", "lineno", "function", "file", "msg")
    open class Wcserr(
        @JvmField var status: Int = 0,
        @JvmField var lineno: Int = 0,
        @JvmField var function: Pointer? = null,
        @JvmField var file: Pointer? = null,
        @JvmField var msg: Pointer? = null,
    ) : Structure() {

        open class ByReference : Wcserr(), Structure.ByReference

        open class ByValue : Wcserr(), Structure.ByValue
    }

    companion object {

        internal val INSTANCE: LibWCS

        init {
            val name = when (val platform = SystemInfo.getCurrentPlatform()) {
                PlatformEnum.LINUX -> "libwcs.so.8.1"
                PlatformEnum.WINDOWS -> "libwcs.dll.8.1"
                else -> throw IllegalStateException("unsupported platform: $platform")
            }

            val outputPath = Files.createTempFile("nebulosa", name)
            resource(name)!!.transferAndClose(outputPath.outputStream())

            INSTANCE = Native.load("$outputPath", LibWCS::class.java)
        }

//        fun pix2sky(
//            naxis: Int,
//            crpix: DoubleArray = DoubleArray(naxis),
//            pc: DoubleArray = DoubleArray(naxis),
//            cdelt: DoubleArray = DoubleArray(naxis),
//            crval: DoubleArray = DoubleArray(naxis),
//            cunit: Array<String> = Array(naxis) { "" },
//            ctype: Array<String> = Array(naxis) { "" },
//            lonpole: DoubleArray = DoubleArray(naxis),
//            latpole: DoubleArray = DoubleArray(naxis),
//            restfrq: DoubleArray = DoubleArray(naxis),
//            restwav: DoubleArray = DoubleArray(naxis),
//            pvi: IntArray = IntArray(naxis),
//            pvm: IntArray = IntArray(naxis),
//            pvv: DoubleArray = DoubleArray(naxis),
//            psi: IntArray = IntArray(naxis),
//            psm: IntArray = IntArray(naxis),
//            psv: Array<String> = Array(naxis) { "" },
//            cd: DoubleArray = DoubleArray(naxis),
//            crota: DoubleArray = DoubleArray(naxis),
//            pixcrd: DoubleArray = DoubleArray(naxis)
//        ): Result {
//            val world = DoubleArray(pixcrd.size)
//            val worldUnits = arrayOfNulls<String>(pixcrd.size)
//
//            val status = INSTANCE.wcsp2s()
//
//            return if (status == 0) {
//                Result(world, worldUnits)
//            } else {
//                var message: String = ERROR_MAP.get(status)
//                if (message == null) {
//                    message = "BUG: unknown status value returned by wcsLibJNI"
//                }
//                throw WCSLibRuntimeException(message, status)
//            }
//        }
    }
}
