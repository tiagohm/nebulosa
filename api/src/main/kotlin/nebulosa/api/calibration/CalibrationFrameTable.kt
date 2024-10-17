package nebulosa.api.calibration

import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.Table

object CalibrationFrameTable : Table("CALIBRATION_FRAMES") {
    val id = long("ID").autoIncrement()
    val type = enumeration<FrameType>("TYPE")
    val group = text("GROUP")
    val filter = text("FILTER").nullable()
    val exposureTime = long("EXPOSURE_TIME")
    val temperature = double("TEMPERATURE")
    val width = integer("WIDTH")
    val height = integer("HEIGHT")
    val binX = integer("BIN_X")
    val binY = integer("BIN_Y")
    val gain = double("GAIN")
    val path = text("PATH")
    val enabled = bool("ENABLED")

    override val primaryKey = PrimaryKey(id)
}
