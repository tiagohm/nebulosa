package nebulosa.fits

import nebulosa.image.format.ImageModifier

private data class WithBitpix(@JvmField val value: Bitpix) : ImageModifier.Element

fun ImageModifier.bitpix(value: Bitpix) = then(WithBitpix(value))

fun ImageModifier.bitpix(): Bitpix? {
    var bitpix: Bitpix? = null
    foldIn { if (it is WithBitpix) bitpix = it.value }
    return bitpix
}
