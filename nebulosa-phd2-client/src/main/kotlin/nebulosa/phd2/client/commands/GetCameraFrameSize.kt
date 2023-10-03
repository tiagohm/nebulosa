package nebulosa.phd2.client.commands

data object GetCameraFrameSize : PHD2Command<IntArray> {

    override val methodName = "get_camera_frame_size"

    override val params = null

    override val responseType = IntArray::class.java
}
