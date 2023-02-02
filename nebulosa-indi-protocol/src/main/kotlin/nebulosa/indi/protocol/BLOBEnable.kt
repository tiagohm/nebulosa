package nebulosa.indi.protocol

enum class BLOBEnable(override val text: String) : HasText {
    NEVER("Never"),
    ALSO("Also"),
    ONLY("Only");

    override fun toString() = text

    companion object {

        @JvmStatic
        fun parse(text: String) = valueOf(text.uppercase())
    }
}
