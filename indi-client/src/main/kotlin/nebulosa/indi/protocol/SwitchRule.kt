package nebulosa.indi.protocol

enum class SwitchRule(override val text: String) : HasText {
    ONE_OF_MANY("OneOfMany"),
    AT_MOST_ONE("AtMostOne"),
    ANY_OF_MANY("AnyOfMany");

    override fun toString() = text
}
