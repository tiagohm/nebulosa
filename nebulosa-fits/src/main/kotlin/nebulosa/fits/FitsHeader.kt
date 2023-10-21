package nebulosa.fits

interface FitsHeader {

    val key: String

    val comment: String?

    val hduType: HduType

    val valueType: ValueType

    fun n(vararg numbers: Int): FitsHeader
}
