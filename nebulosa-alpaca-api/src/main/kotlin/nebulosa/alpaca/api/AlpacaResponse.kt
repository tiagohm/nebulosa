package nebulosa.alpaca.api

sealed interface AlpacaResponse<out T> {

    val clientTransactionID: Int

    val serverTransactionID: Int

    val errorNumber: Int

    val errorMessage: String

    val value: T
}
