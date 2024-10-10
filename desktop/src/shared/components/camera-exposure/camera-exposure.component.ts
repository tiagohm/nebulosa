import { Component, Input } from '@angular/core'
import { CameraCaptureEvent, CameraCaptureState, DEFAULT_CAMERA_CAPTURE_INFO, DEFAULT_CAMERA_STEP_INFO } from '../../types/camera.types'

@Component({
	selector: 'neb-camera-exposure',
	templateUrl: './camera-exposure.component.html',
	styleUrls: ['./camera-exposure.component.scss'],
})
export class CameraExposureComponent {
	@Input()
	protected info?: string

	@Input()
	protected showRemainingTime: boolean = true

	@Input()
	protected readonly step = structuredClone(DEFAULT_CAMERA_STEP_INFO)

	@Input()
	protected readonly capture = structuredClone(DEFAULT_CAMERA_CAPTURE_INFO)

	protected state: CameraCaptureState = 'IDLE'

	get currentState() {
		return this.state
	}

	handleCameraCaptureEvent(event: Omit<CameraCaptureEvent, 'camera'>, looping: boolean = false) {
		this.capture.elapsedTime = event.captureElapsedTime
		this.capture.remainingTime = event.captureRemainingTime
		this.capture.progress = event.captureProgress
		this.capture.count = event.exposureCount
		this.capture.amount = event.exposureAmount
		if (looping) this.capture.looping = looping
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
		} else if (event.state === 'IDLE' || event.state === 'CAPTURE_FINISHED') {
			this.reset()
		} else if (event.state !== 'EXPOSURE_FINISHED') {
			this.state = event.state
		}

		return this.state !== 'CAPTURE_FINISHED' && this.state !== 'IDLE'
	}

	reset() {
		this.state = 'IDLE'

		Object.assign(this.step, DEFAULT_CAMERA_STEP_INFO)
		Object.assign(this.capture, DEFAULT_CAMERA_CAPTURE_INFO)
	}
}
