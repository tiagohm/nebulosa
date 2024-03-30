package nebulosa.astrobin.api

@Suppress("ArrayInDataClass")
data class Sensor(
    override val id: Long = 0L,
    override val brandName: String = "",
    override val name: String = "",
    val quantumEfficiency: Double = 0.0,
    val pixelSize: Double = 0.0,
    val pixelWidth: Int = 0,
    val pixelHeight: Int = 0,
    val readNoise: Double = 0.0,
    val fullWellCapacity: Double = 0.0,
    val adc: Int = 0,
    val color: SensorColor = SensorColor.MONO,
    val cameras: LongArray = LongArray(0),
) : Equipment
