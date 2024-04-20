package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.util.*

data class DARVTaskContext(
    @JvmField val camera: Camera,
    @JvmField val guideOutput: GuideOutput,
    @JvmField val request: DARVStartRequest,
    @JvmField val cameraRequest: CameraStartCaptureRequest,
    @JvmField val cancellationToken: CancellationToken,
) {

    @JvmField val id = UUID.randomUUID().toString()
    @JvmField var cameraCaptureTaskId = ""
    @JvmField var cameraCaptureTaskSubscription: Disposable? = null
}
