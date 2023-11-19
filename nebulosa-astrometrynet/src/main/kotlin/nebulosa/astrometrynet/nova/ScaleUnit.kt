package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonValue

enum class ScaleUnit(@field:JsonValue private val value: String) {
    DEGREES_WIDTH("degwidth"),
    ARCMIN_WIDTH("arcminwidth"),
    ARCSEC_PER_PIX("arcsecperpix"),
}
