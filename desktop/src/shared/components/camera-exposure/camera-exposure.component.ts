import { Component, Input } from '@angular/core'
import { CameraCaptureElapsed, CameraCaptureState, EMPTY_CAMERA_CAPTURE_INFO, EMPTY_CAMERA_STEP_INFO } from '../../types/camera.types'

@Component({
    selector: 'neb-camera-exposure',
    templateUrl: './camera-exposure.component.html',
    styleUrls: ['./camera-exposure.component.scss'],
})
export class CameraExposureComponent {

    @Input()
    state?: CameraCaptureState = 'IDLE'

    @Input()
    showRemainingTime: boolean = true

    @Input()
    readonly step = structuredClone(EMPTY_CAMERA_STEP_INFO)

    @Input()
    readonly capture = structuredClone(EMPTY_CAMERA_CAPTURE_INFO)

    handleCameraCaptureEvent(event: CameraCaptureElapsed, looping: boolean = false) {
        this.capture.elapsedTime = event.captureElapsedTime
        this.capture.remainingTime = event.captureRemainingTime
        this.capture.progress = event.captureProgress
        this.capture.count = event.exposureCount
        this.step.elapsedTime = event.stepElapsedTime
        this.step.remainingTime = event.stepRemainingTime
        this.step.progress = event.stepProgress

        if (event.state === 'EXPOSURING') {
            this.state = 'EXPOSURING'
        } else if (event.state === 'WAITING') {
            this.step.elapsedTime = event.stepElapsedTime
            this.step.remainingTime = event.stepRemainingTime
            this.step.progress = event.stepProgress
            this.state = event.state
        } else if (event.state === 'CAPTURE_STARTED') {
            this.capture.looping = looping || event.exposureAmount <= 0
            this.capture.amount = event.exposureAmount
            this.state = 'EXPOSURING'
        } else if (event.state === 'EXPOSURE_STARTED') {
            this.state = 'EXPOSURING'
        } else if (event.state === 'IDLE' ||
            (!looping && event.state === 'CAPTURE_FINISHED') ||
            (!this.capture.looping && !this.capture.remainingTime)) {
            this.state = 'IDLE'
        }

        return this.state !== undefined && this.state !== 'CAPTURE_FINISHED'
            && this.state !== 'IDLE' && !event.aborted
    }

    reset() {
        this.state = 'IDLE'

        Object.assign(this.step, EMPTY_CAMERA_STEP_INFO)
        Object.assign(this.capture, EMPTY_CAMERA_CAPTURE_INFO)
    }
}