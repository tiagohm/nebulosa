@file:JvmName("Files")

package nebulosa.test

import nebulosa.math.deg
import nebulosa.math.hours

const val ASTROPY_PHOTOMETRY_URL = "https://www.astropy.org/astropy-data/photometry"
const val GITHUB_FITS_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/fits"
const val GITHUB_XISF_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/xisf"

val M82_MONO_8_LZ4_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.LZ4.xisf") }
val M82_MONO_8_LZ4_HC_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.LZ4-HC.xisf") }
val M82_MONO_8_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.xisf") }
val M82_MONO_8_ZLIB_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.ZLib.xisf") }
val M82_MONO_8_ZSTANDARD_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.ZStandard.xisf") }
val M82_MONO_16_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.16.xisf") }
val M82_MONO_32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.32.xisf") }
val M82_MONO_F32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.F32.xisf") }
val M82_MONO_F64_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.F64.xisf") }

val M82_COLOR_8_LZ4_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.LZ4.xisf") }
val M82_COLOR_8_LZ4_HC_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.LZ4-HC.xisf") }
val M82_COLOR_8_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.xisf") }
val M82_COLOR_8_ZLIB_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.ZLib.xisf") }
val M82_COLOR_8_ZSTANDARD_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.ZStandard.xisf") }
val M82_COLOR_16_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.16.xisf") }
val M82_COLOR_32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.32.xisf") }
val M82_COLOR_F32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.F32.xisf") }
val M82_COLOR_F64_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.F64.xisf") }
val DEBAYER_XISF_PATH by lazy { download("$GITHUB_XISF_URL/Debayer.xisf") }

val NGC3344_MONO_8_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.8.fits") }
val NGC3344_MONO_16_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.16.fits") }
val NGC3344_MONO_32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.32.fits") }
val NGC3344_MONO_F32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.F32.fits") }
val NGC3344_MONO_F64_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.F64.fits") }

val NGC3344_COLOR_8_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.8.fits") }
val NGC3344_COLOR_16_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.16.fits") }
val NGC3344_COLOR_32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.32.fits") }
val NGC3344_COLOR_F32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.F32.fits") }
val NGC3344_COLOR_F64_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.F64.fits") }

val PALETTE_MONO_8_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.8.fits") }
val PALETTE_MONO_16_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.16.fits") }
val PALETTE_MONO_32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.32.fits") }
val PALETTE_MONO_F32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.F32.fits") }
val PALETTE_MONO_F64_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.F64.fits") }

val PALETTE_COLOR_8_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.8.fits") }
val PALETTE_COLOR_16_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.16.fits") }
val PALETTE_COLOR_32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.32.fits") }
val PALETTE_COLOR_F32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.F32.fits") }
val PALETTE_COLOR_F64_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.F64.fits") }

val PALETTE_MONO_8_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.8.xisf") }
val PALETTE_MONO_16_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.16.xisf") }
val PALETTE_MONO_32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.32.xisf") }
val PALETTE_MONO_F32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.F32.xisf") }
val PALETTE_MONO_F64_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.F64.xisf") }

val PALETTE_COLOR_8_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.8.xisf") }
val PALETTE_COLOR_16_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.16.xisf") }
val PALETTE_COLOR_32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.32.xisf") }
val PALETTE_COLOR_F32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.F32.xisf") }
val PALETTE_COLOR_F64_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.F64.xisf") }

val DEBAYER_FITS by lazy { download("$GITHUB_FITS_URL/Debayer.fits") }
val M6707HH by lazy { download("$ASTROPY_PHOTOMETRY_URL/M6707HH.fits") }
val M31_FITS by lazy { downloadFits("00 42 44.3".hours, "41 16 9".deg, 3.deg) }
