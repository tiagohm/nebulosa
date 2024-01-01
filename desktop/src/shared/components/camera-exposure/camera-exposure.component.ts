import { Component, Input } from '@angular/core'
import { CameraCaptureEvent, CameraCaptureState, EMPTY_CAMERA_CAPTURE_INFO, EMPTY_CAMERA_EXPOSURE_INFO, EMPTY_CAMERA_WAIT_INFO } from '../../types/camera.types'

@Component({
    selector: 'neb-camera-exposure',
    templateUrl: './camera-exposure.component.html',
    styleUrls: ['./camera-exposure.component.scss'],
})
export class CameraExposureComponent {

    @Input()
    state?: CameraCaptureState = 'IDLE'

    @Input()
    readonly exposure = Object.assign({}, EMPTY_CAMERA_EXPOSURE_INFO)

    @Input()
    readonly capture = Object.assign({}, EMPTY_CAMERA_CAPTURE_INFO)

    @Input()
    readonly wait = Object.assign({}, EMPTY_CAMERA_WAIT_INFO)

    handleCameraCaptureEvent(event: CameraCaptureEvent) {
        this.capture.elapsedTime = event.captureElapsedTime
        this.capture.remainingTime = event.captureRemainingTime
        this.capture.progress = event.captureProgress
        this.exposure.remainingTime = event.exposureRemainingTime
        this.exposure.progress = event.exposureProgress
        this.exposure.count = event.exposureCount

        if (event.state === 'WAITING') {
            this.wait.remainingTime = event.waitRemainingTime
            this.wait.progress = event.waitProgress
            this.state = event.state
        } else if (event.state === 'SETTLING') {
            this.state = event.state
        } else if (event.state === 'CAPTURE_STARTED') {
            this.capture.looping = event.exposureAmount <= 0
            this.capture.amount = event.exposureAmount
            this.state = 'EXPOSURING'
        } else if (event.state === 'EXPOSURE_STARTED') {
            this.state = 'EXPOSURING'
        } else if (event.state === 'CAPTURE_FINISHED' || !this.capture.remainingTime) {
            this.state = 'IDLE'
        }

        return this.state !== undefined && this.state !== 'CAPTURE_FINISHED' && this.state !== 'IDLE'
    }

    reset() {
        this.state = 'IDLE'

        Object.assign(this.exposure, EMPTY_CAMERA_EXPOSURE_INFO)
        Object.assign(this.capture, EMPTY_CAMERA_CAPTURE_INFO)
        Object.assign(this.wait, EMPTY_CAMERA_WAIT_INFO)
    }
}