package nebulosa.phd2.client.commands

import com.fasterxml.jackson.annotation.JsonValue

enum class ShiftAxesType(
    @JsonValue val axes: String,
    val rateUnit: RateUnit,
) {
    RADEC("RA/Dec", RateUnit.ARCSEC_HOUR),
    XY("X/Y", RateUnit.PIXELS_HOUR),
}
