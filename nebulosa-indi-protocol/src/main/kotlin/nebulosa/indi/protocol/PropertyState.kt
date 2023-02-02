package nebulosa.indi.protocol

enum class PropertyState(override val text: String) : HasText {
    IDLE("Idle"),
    OK("Ok"),
    BUSY("Busy"),
    ALERT("Alert");

    override fun toString() = text

    companion object {

        @JvmStatic
        fun parse(text: String) = valueOf(text.uppercase())
    }
}
