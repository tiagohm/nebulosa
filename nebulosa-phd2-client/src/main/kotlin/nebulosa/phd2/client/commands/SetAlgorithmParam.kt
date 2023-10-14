package nebulosa.phd2.client.commands

data class SetAlgorithmParam(
    val axis: String,
    val name: String, val value: Any,
) : PHD2Command<Int> {

    override val methodName = "set_algo_param"

    override val params = listOf(axis, name, value)

    override val responseType = Int::class.java
}
