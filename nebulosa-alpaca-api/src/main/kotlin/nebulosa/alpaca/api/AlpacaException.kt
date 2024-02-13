package nebulosa.alpaca.api

import retrofit2.HttpException
import retrofit2.Response

open class AlpacaException(response: Response<out AlpacaResponse<*>>) : HttpException(response)
