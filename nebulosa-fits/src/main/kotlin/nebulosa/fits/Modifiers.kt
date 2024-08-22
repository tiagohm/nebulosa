package nebulosa.fits

import nebulosa.image.format.ImageModifier

private data class Bitpixed(@JvmField val value: Bitpix) : ImageModifier.Element

fun ImageModifier.bitpix(value: Bitpix) = then(Bitpixed(value))

fun ImageModifier.bitpix(): Bitpix? {
    var bitpix: Bitpix? = null
    foldIn { if (it is Bitpixed) bitpix = it.value }
    return bitpix
}
