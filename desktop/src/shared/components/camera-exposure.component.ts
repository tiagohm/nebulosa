import { Component, input, model, ViewEncapsulation } from '@angular/core'
import type { CameraCaptureEvent, CameraCaptureState } from '../types/camera.types'
import { DEFAULT_CAMERA_CAPTURE_INFO, DEFAULT_CAMERA_STEP_INFO } from '../types/camera.types'

@Component({
	standalone: false,
	selector: 'neb-camera-exposure',
	template: `
		<div class="flex flex-col items-start justify-center gap-1">
			<span class="flex items-center gap-1 text-left">
				<i class="mdi mdi-sm mdi-information text-blue-500"></i>
				{{ info() || state || 'IDLE' | enum | lowercase }}
			</span>
			<span class="flex gap-[1px]">
				<span class="state flex min-w-28 items-center justify-center gap-1 bg-orange-500 text-gray-900">
					<i class="mdi mdi-sm mdi-counter"></i>
					{{ capture.count }}
					@if (!capture.looping) {
						<span>/ {{ capture.amount }}</span>
					}
				</span>
				@if (!capture.looping) {
					<span class="state flex min-w-24 items-center justify-center gap-1 bg-green-500 text-gray-900">
						<i class="mdi mdi-sm mdi-percent"></i>
						{{ capture.progress * 100 | number: '1.1-1' }}
					</span>
				}
				@if (capture.looping) {
					<span class="state flex min-w-24 items-center justify-center gap-1 bg-blue-500 text-gray-900">
						<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
						{{ capture.elapsedTime | exposureTime }}
					</span>
				} @else {
					<span
						class="state flex min-w-24 cursor-pointer items-center justify-center gap-1 bg-blue-500 text-gray-900"
						(click)="showRemainingTime.set(!showRemainingTime())">
						@if (showRemainingTime()) {
							<span class="flex items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand"></i>
								{{ capture.remainingTime | exposureTime }}
							</span>
						} @else {
							<span class="flex items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
								{{ capture.elapsedTime | exposureTime }}
							</span>
						}
					</span>
				}
				@if (capture.amount !== 1 && (state === 'EXPOSURING' || state === 'WAITING')) {
					<span
						class="state flex min-w-24 cursor-pointer items-center justify-center gap-1 bg-cyan-300 text-gray-900"
						(click)="showRemainingTime.set(!showRemainingTime())">
						@if (showRemainingTime()) {
							<span class="flex items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand"></i>
								{{ step.remainingTime | exposureTime }}
							</span>
						} @else {
							<span class="flex items-center gap-1">
								<i class="mdi mdi-sm mdi-timer-sand-complete"></i>
								{{ step.elapsedTime | exposureTime }}
							</span>
						}
					</span>
					<span class="state flex min-w-20 items-center justify-center gap-1 bg-cyan-300 text-gray-900">
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
