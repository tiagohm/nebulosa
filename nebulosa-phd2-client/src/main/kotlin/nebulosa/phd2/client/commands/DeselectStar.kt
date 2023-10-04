package nebulosa.phd2.client.commands

data object DeselectStar : PHD2Command<Int> {

    override val methodName = "deselect_star"

    override val params = null

    override val responseType = Int::class.java
}
