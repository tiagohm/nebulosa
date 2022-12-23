package nebulosa.server.profile

data class Profile(
    val imagingCamera: String = "",
    val mount: String = "",
    val guidingCamera: String = "",
    val filterWheel: String = "",
    val focuser: String = "",
    val rotator: String = "",
    val switch: String = "",
    val dome: String = "",
    val weather: String = "",
    val flatPanel: String = "",
    val safetyMonitor: String = "",
)
