package nebulosa.pixinsight.script

sealed interface PixInsightScriptOutput {

    val success: Boolean

    val errorMessage: String?
}
