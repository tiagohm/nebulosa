package nebulosa.jna

import nebulosa.io.resource
import java.io.InputStream

interface LibraryProvider {

    val libraryName: String

    fun provideStream(prefix: String, extension: String): InputStream? {
        return resource("$prefix/$libraryName$extension")
    }
}
