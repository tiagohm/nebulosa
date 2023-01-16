package nebulosa.indi.protocol

enum class SwitchState(override val text: String) : HasText {
    OFF("Off"),
    ON("On");

    override fun toString() = text
}
