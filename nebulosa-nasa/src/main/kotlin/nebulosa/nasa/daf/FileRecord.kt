package nebulosa.nasa.daf

import nebulosa.io.ByteOrder

data class FileRecord(
    val nd: Int,
    val ni: Int,
    val fward: Int,
    val bward: Int,
    val order: ByteOrder,
)
