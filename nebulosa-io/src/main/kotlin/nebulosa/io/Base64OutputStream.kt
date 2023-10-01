package nebulosa.io

import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64

// TODO: Improve it encoding on write (as same Base64InputStream).
// TODO: Base64OutputStream(outputStream, urlSafe): OutputStream
open class Base64OutputStream(size: Int) : ByteArrayOutputStream(size) {

    fun base64() = Base64.encode(buf, 0, count)

    fun base64UrlSafe() = Base64.UrlSafe.encode(buf, 0, count)
}
