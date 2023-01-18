package nebulosa.alpaca.client

import retrofit2.HttpException
import retrofit2.Response

class AlpacaException(response: Response<out AlpacaResponse<*>>) : HttpException(response)
