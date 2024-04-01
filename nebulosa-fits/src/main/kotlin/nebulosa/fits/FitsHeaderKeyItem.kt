package nebulosa.fits

data class FitsHeaderKeyItem(
    override val key: String,
    override val hduType: HduType,
    override val valueType: ValueType,
    override val comment: String = "",
) : FitsHeaderKey {

    override fun n(vararg numbers: Int): FitsHeaderKey {
        if (numbers.isEmpty() || "n" !in key) return this

        val key = StringBuffer(key)
        var idx = 0

        for (number in numbers) {
            idx = key.indexOf("n", idx)
            if (idx < 0) break
            key.replace(idx, idx + 1, "$number")
        }

        return FitsHeaderKeyItem("$key", hduType, valueType, comment)
    }
}
