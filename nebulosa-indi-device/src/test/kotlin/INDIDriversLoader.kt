import com.fasterxml.aalto.stax.InputFactoryImpl
import nebulosa.xml.attribute
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.stream.XMLStreamConstants

object INDIDriversLoader {

    const val URL = "https://raw.githubusercontent.com/KDE/kstars/master/kstars/data/indidrivers.xml"

    @JvmStatic
    fun main(args: Array<String>) {
        val request = Request.Builder().get().url(URL).build()

        HTTP_CLIENT.newCall(request).execute().use { response ->
            val reader = with(Vector<InputStream>()) {
                add("<Nebulosa>".byteInputStream())
                add(response.body!!.byteStream().also { it.skip(38) })
                add("</Nebulosa>".byteInputStream())
                val source = SequenceInputStream(elements())
                XML_INPUT_FACTORY.createXMLStreamReader(source)
            }

            var group = ""
            val devices = HashMap<String, MutableCollection<String>>(8)

            while (reader.hasNext()) {
                when (reader.next()) {
                    XMLStreamConstants.START_ELEMENT -> {
                        when (reader.localName) {
                            "devGroup" -> group = reader.attribute("group")!!.uppercase()
                            "driver" -> devices.getOrPut(group, ::TreeSet).add(reader.elementText)
                        }
                    }
                }
            }

            for ((name, drivers) in devices) {
                println(name)

                for (driver in drivers) {
                    println("\"$driver\",")
                }

                println()
            }
        }
    }

    @JvmStatic private val XML_INPUT_FACTORY = InputFactoryImpl()

    @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
        .connectTimeout(5L, TimeUnit.MINUTES)
        .writeTimeout(5L, TimeUnit.MINUTES)
        .readTimeout(5L, TimeUnit.MINUTES)
        .callTimeout(5L, TimeUnit.MINUTES)
        .build()
}
