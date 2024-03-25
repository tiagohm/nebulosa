package nebulosa.fits

data class FitsKeywordItem(
    override val key: String,
    override val hduType: HduType,
    override val valueType: ValueType,
    override val comment: String = "",
) : FitsKeyword {

    override fun n(vararg numbers: Int): FitsKeyword {
        if (numbers.isEmpty() || "n" !in key) return this

        val key = StringBuffer(key)

        for (number in numbers) {
            val idx = key.indexOf("n")
            key.replace(idx, idx + 1, "$number")
        }

        return FitsKeywordItem("$key", hduType, valueType, comment)
    }
}
