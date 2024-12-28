import { Component, input, model, ViewEncapsulation } from '@angular/core'
import { CameraCaptureEvent, CameraCaptureState, DEFAULT_CAMERA_CAPTURE_INFO, DEFAULT_CAMERA_STEP_INFO } from '../types/camera.types'

@Component({
	standalone: false,
	selector: 'neb-camera-exposure',
	template: `
		<div class="flex flex-column align-items-start justify-content-center gap-1">
			<span class="text-left flex align-items-center gap-1">
				<i class="mdi mdi-sm mdi-information text-info"></i>
				{{ info() || state || 'IDLE' | enum | lowercase }}
			</span>
			<span class="flex flex-row gap-1px">
				<span class="bg-warning state min-w-7rem flex align-items-center gap-1 justify-content-center text-gray-900">
					<i class="mdi mdi-sm mdi-counter"></i>
					{{ capture.count }}
					@if (!capture.looping) {
						<span>/ {{ capture.amount }}</span>
					}
				</span>
				@if (!capture.looping) {
					<span class="bg-success state min-w-6rem flex align-items-center gap-1 justify-content-center text-gray-900">
						<i class="mdi mdi-sm mdi-percent"></i>
						{{ capture.progress * 100 | number: '1.1-1' }}
					</span>
				}
				@if (capture.looping) {
					<span class="bg-info state min-w-6rem flex align-items-center gap-1 justify-content-center text-gray-900">
						<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
						{{ capture.elapsedTime | exposureTime }}
					</span>
				} @else {
					<span
						class="bg-info state min-w-6rem flex align-items-center gap-1 justify-content-center text-gray-900 cursor-pointer"
						(click)="showRemainingTime.set(!showRemainingTime())">
						@if (showRemainingTime()) {
							<span class="flex align-items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand"></i>
								{{ capture.remainingTime | exposureTime }}
							</span>
						} @else {
							<span class="flex align-items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
								{{ capture.elapsedTime | exposureTime }}
							</span>
						}
					</span>
				}
				@if (capture.amount !== 1 && (state === 'EXPOSURING' || state === 'WAITING')) {
					<span
						class="bg-cyan-300 state min-w-6rem flex align-items-center gap-1 justify-content-center text-gray-900 cursor-pointer"
						(click)="showRemainingTime.set(!showRemainingTime())">
						@if (showRemainingTime()) {
							<span class="flex align-items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand"></i>
								{{ step.remainingTime | exposureTime }}
							</span>
						} @else {
							<span class="flex align-items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
								{{ step.elapsedTime | exposureTime }}
							</span>
						}
					</span>
					<span class="bg-cyan-300 state min-w-5rem flex align-items-center gap-1 justify-content-center text-gray-900">
						<i class="mdi mdi-sm mdi-percent"></i>
						{{ step.progress * 100 | number: '1.1-1' }}
					</span>
				}
			</span>
		</div>
	`,
	styles: `
		neb-camera-exposure {
			min-height: 29px;
			width: 100%;

			.state {
				padding: 1px 6px;
				height: 13px;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class CameraExposureComponent {
	readonly info = input<string>()
	readonly showRemainingTime = model(true)

	protected step = structuredClone(DEFAULT_CAMERA_STEP_INFO)
	protected capture = structuredClone(DEFAULT_CAMERA_CAPTURE_INFO)
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

		this.step = structuredClone(DEFAULT_CAMERA_STEP_INFO)
		this.capture = structuredClone(DEFAULT_CAMERA_CAPTURE_INFO)
	}
}
