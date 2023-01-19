package nebulosa.alpaca.client

import nebulosa.alpaca.api.AlpacaResponse
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun interface DeviceCallback<T> : Callback<AlpacaResponse<T>> {

    fun onSuccess(data: T?)

    override fun onFailure(call: Call<AlpacaResponse<T>>, e: Throwable) {
        LOG.error("device callback error", e)
    }

    override fun onResponse(call: Call<AlpacaResponse<T>>, response: Response<AlpacaResponse<T>>) {
        if (response.isSuccessful) {
            onSuccess(response.body()?.value)
        } else {
            LOG.error("device callback failed with code {}", response.code())
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(DeviceCallback::class.java)
    }
}
