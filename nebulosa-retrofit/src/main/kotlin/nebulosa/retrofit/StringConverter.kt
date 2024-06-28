package nebulosa.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter

data object StringConverter : Converter<ResponseBody, String> {

    override fun convert(value: ResponseBody) = value.string()
}
