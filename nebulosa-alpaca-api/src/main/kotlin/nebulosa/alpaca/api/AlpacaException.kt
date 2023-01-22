package nebulosa.alpaca.api

import retrofit2.HttpException
import retrofit2.Response

class AlpacaException(response: Response<out AlpacaResponse<*>>) : HttpException(response)
