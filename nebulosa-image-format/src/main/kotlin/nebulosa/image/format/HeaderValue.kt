package nebulosa.image.format

interface HeaderValue {

    val value: String

    fun <T> getValue(asType: Class<out T>, defaultValue: T): T

    fun formatted(): String
}
