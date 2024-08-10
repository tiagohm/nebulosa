@file:JvmName("Files")

package nebulosa.test

import nebulosa.math.deg
import nebulosa.math.hours

const val ASTROPY_PHOTOMETRY_URL = "https://www.astropy.org/astropy-data/photometry"
const val GITHUB_FITS_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/test/fits"
const val GITHUB_XISF_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/test/xisf"

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

val STAR_FOCUS_1 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.1.fits") }
val STAR_FOCUS_2 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.2.fits") }
val STAR_FOCUS_3 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.3.fits") }
val STAR_FOCUS_4 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.4.fits") }
val STAR_FOCUS_5 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.5.fits") }
val STAR_FOCUS_6 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.6.fits") }
val STAR_FOCUS_7 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.7.fits") }
val STAR_FOCUS_8 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.8.fits") }
val STAR_FOCUS_9 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.9.fits") }
val STAR_FOCUS_10 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.10.fits") }
val STAR_FOCUS_11 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.11.fits") }
val STAR_FOCUS_12 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.12.fits") }
val STAR_FOCUS_13 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.13.fits") }
val STAR_FOCUS_14 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.14.fits") }
val STAR_FOCUS_15 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.15.fits") }
val STAR_FOCUS_16 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.16.fits") }
val STAR_FOCUS_17 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.17.fits") }

val PI_01_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.01.LIGHT.fits") }
val PI_02_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.02.LIGHT.fits") }
val PI_03_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.03.LIGHT.fits") }
val PI_04_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.04.LIGHT.fits") }
val PI_05_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.05.LIGHT.fits") }
val PI_06_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.06.LIGHT.fits") }
val PI_07_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.07.LIGHT.fits") }
val PI_08_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.08.LIGHT.fits") }
val PI_09_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.09.LIGHT.fits") }
val PI_10_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.10.LIGHT.fits") }
val PI_BIAS by lazy { download("$GITHUB_FITS_URL/PI.BIAS.fits") }
val PI_DARK by lazy { download("$GITHUB_FITS_URL/PI.DARK.fits") }
val PI_FLAT by lazy { download("$GITHUB_FITS_URL/PI.FLAT.fits") }

val PI_FOCUS_0 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.0.fits") }
val PI_FOCUS_10000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.10000.fits") }
val PI_FOCUS_20000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.20000.fits") }
val PI_FOCUS_30000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.30000.fits") }
val PI_FOCUS_100000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.100000.fits") }
