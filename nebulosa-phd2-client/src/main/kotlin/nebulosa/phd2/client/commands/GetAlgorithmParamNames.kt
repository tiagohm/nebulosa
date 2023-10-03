package nebulosa.phd2.client.commands

data class GetAlgorithmParamNames(val axis: String) : PHD2Command<Array<String>> {

    override val methodName = "get_algo_param_names"

    override val params = mapOf("axis" to axis)

    override val responseType = Array<String>::class.java
}
