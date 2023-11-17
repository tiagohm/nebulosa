package nebulosa.fits

data class FitsHeaderImpl(
    override val key: String,
    override val hduType: HduType,
    override val valueType: ValueType,
    override val comment: String = "",
) : FitsHeader {

    override fun n(vararg numbers: Int): FitsHeader {
        if (numbers.isEmpty() || "n" !in key) return this

        val key = StringBuffer(key)

        for (number in numbers) {
            val idx = key.indexOf("n")
            key.replace(idx, idx + 1, "$number")
        }

        return FitsHeaderImpl("$key", hduType, valueType, comment)
    }
}
