package nebulosa.phd2.client.commands

data class GetAlgorithmParam(val axis: String, val name: String) : PHD2Command<Any> {

    override val methodName = "get_algo_param"

    override val params = mapOf("axis" to axis, "name" to name)

    override val responseType = Any::class.java
}
