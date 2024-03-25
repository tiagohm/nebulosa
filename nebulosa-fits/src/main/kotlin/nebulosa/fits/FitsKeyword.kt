package nebulosa.fits

import nebulosa.image.format.HeaderKey

interface FitsKeyword : HeaderKey {

    val hduType: HduType

    val valueType: ValueType

    fun n(vararg numbers: Int): FitsKeyword

    val isCommentStyle
        get() = valueType == ValueType.NONE || key.isBlank()
}
