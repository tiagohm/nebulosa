import io.kotest.core.spec.style.StringSpec
import nebulosa.wcs.LibWCS

// https://github.com/opencadc/wcs/blob/master/cadc-wcs/src/wcsLibJNI/c/wcslib.c
// https://github.com/opencadc/wcs/blob/master/cadc-wcs/src/main/java/ca/nrc/cadc/wcs/WCSLib.java

class LibWCSTest : StringSpec() {

    init {
        // Image Plate Solver script version 5.6.6
        // ===============================================================================
        // Referentiation matrix (world[ra,dec] = matrix * image[x,y]):
        // -1.30976816e-04  +3.57262167e-04  -1.16360018e-01
        // -3.57067457e-04  -1.30892458e-04  +4.62258498e-01
        // WCS transformation ....... Linear
        // Projection ............... Gnomonic
        // Projection origin ........ [1035.977006 705.501479] px -> [RA: 10 44 04.269  Dec: -59 36 07.85]
        // Resolution ............... 1.369 arcsec/px
        // Rotation ................. -69.879 deg
        // Reference system ......... ICRS
        // Observation start time ... 2023-01-15 01:27:05 UTC
        // Observation end time ..... 2023-01-15 01:27:35 UTC
        // Geodetic coordinates .....  45 29 16 W  22 30 35 S
        // Focal distance ........... 1394.71 mm
        // Pixel size ............... 9.26 um
        // Field of view ............ 47' 17.5" x 32' 12.3"
        // Image center ............. RA: 10 44 04.267  Dec: -59 36 07.88
        // Image bounds:
        // top-left .............. RA: 10 43 09.828  Dec: -59 08 23.05
        // top-right ............. RA: 10 40 58.839  Dec: -59 52 39.01
        // bottom-left ........... RA: 10 47 06.658  Dec: -59 19 20.65
        // bottom-right .......... RA: 10 45 00.225  Dec: -60 03 51.26
        "pix2sky" {
            
        }
    }
}
