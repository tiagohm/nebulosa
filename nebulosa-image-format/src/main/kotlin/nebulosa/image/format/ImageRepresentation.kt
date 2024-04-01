package nebulosa.image.format

interface ImageRepresentation : MutableList<Hdu<*>>, ImageSource, ImageSink {

    fun addFirst(hdu: Hdu<*>)

    fun addLast(hdu: Hdu<*>)

    fun removeFirst(): Hdu<*>

    fun removeLast(): Hdu<*>
}
