package nebulosa.pixinsight.script

sealed interface PixInsightOutput {

    val success: Boolean

    val errorMessage: String?
}
