package nebulosa.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter

object ByteArrayConverter : Converter<ResponseBody, ByteArray> {

    override fun convert(value: ResponseBody) = value.bytes()
}
