package nebulosa.indi.protocol

enum class PropertyPermission(override val text: String) : HasText {
    RO("ro"),
    RW("rw"),
    WO("wo");

    override fun toString() = text
}
