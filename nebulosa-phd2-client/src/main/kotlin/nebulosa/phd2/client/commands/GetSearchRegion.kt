package nebulosa.phd2.client.commands

data object GetSearchRegion : PHD2Command<Int> {

    override val methodName = "get_search_region"

    override val params = null

    override val responseType = Int::class.java
}
