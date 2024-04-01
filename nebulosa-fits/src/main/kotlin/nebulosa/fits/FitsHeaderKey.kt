package nebulosa.fits

import nebulosa.image.format.HeaderKey

sealed interface FitsHeaderKey : HeaderKey {

    val hduType: HduType

    val valueType: ValueType

    fun n(vararg numbers: Int): FitsHeaderKey

    val isCommentStyle
        get() = valueType == ValueType.NONE || key.isBlank()
}
