package nebulosa.test.fits

import nebulosa.test.GITHUB_FITS_URL
import nebulosa.test.download

val ASTROMETRY_GALACTIC_CENTER_FITS by lazy { download("$GITHUB_FITS_URL/astrometry/galactic-center.fits") }
val ASTROMETRY_SOUTH_POLE_FITS by lazy { download("$GITHUB_FITS_URL/astrometry/south-celestial-pole.fits") }
val ASTROMETRY_NORTH_POLE_FITS by lazy { download("$GITHUB_FITS_URL/astrometry/north-celestial-pole.fits") }
