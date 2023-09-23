package nebulosa.wcs

open class WCSException(message: String, val status: Int) : RuntimeException("[$status]: $message")
