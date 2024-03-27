package nebulosa.indi.device.camera

import nebulosa.image.format.ImageRepresentation

data class CameraFrameCaptured(
    override val device: Camera,
    @JvmField val image: ImageRepresentation,
    @JvmField val compressed: Boolean,
    @JvmField val format: Format,
) : CameraEvent {

    enum class Format(@JvmField val extension: String) {
        FITS("fits"),
        XISF("xisf"),
        RAW("bin");

        companion object {

            @JvmStatic
            fun from(format: String) = entries.first { format.contains(it.extension, true) }
        }
    }
}
