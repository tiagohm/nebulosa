package nebulosa.indi.protocol

enum class PropertyPermission(override val text: String) : HasText {
    RO("ro"),
    RW("rw"),
    WO("wo");

    override fun toString() = text

    companion object {

        @JvmStatic
        fun parse(text: String) = valueOf(text.uppercase())
    }
}
