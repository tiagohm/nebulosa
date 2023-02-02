package nebulosa.indi.protocol

enum class SwitchRule(override val text: String) : HasText {
    ONE_OF_MANY("OneOfMany"),
    AT_MOST_ONE("AtMostOne"),
    ANY_OF_MANY("AnyOfMany");

    override fun toString() = text

    companion object {

        @JvmStatic
        fun parse(text: String) = when {
            text.equals("OneOfMany", true) -> ONE_OF_MANY
            text.equals("AtMostOne", true) -> AT_MOST_ONE
            text.equals("AnyOfMany", true) -> ANY_OF_MANY
            else -> throw IllegalArgumentException("invalid SwitchRule value: $text")
        }
    }
}
