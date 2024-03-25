package nebulosa.image.format

interface HeaderCard : HeaderKey, HeaderValue, Map.Entry<String, String> {

    val isCommentStyle: Boolean

    val isKeyValuePair: Boolean

    val isBooleanType: Boolean

    val isStringType: Boolean

    val isDecimalType: Boolean

    val isIntegerType: Boolean

    val isNumericType
        get() = isDecimalType || isIntegerType

    val isBlank: Boolean

    companion object {

        inline fun <reified T> HeaderCard.getValue(defaultValue: T): T {
            return getValue(T::class.java, defaultValue)
        }
    }
}
