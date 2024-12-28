import { Component, ViewEncapsulation, input, output } from '@angular/core'
import type { CameraStartCapture } from '../types/camera.types'
import type { Focuser } from '../types/focuser.types'
import type { Rotator } from '../types/rotator.types'
import type { Wheel } from '../types/wheel.types'

@Component({
	standalone: false,
	selector: 'neb-camera-info',
	template: `
		<div class="flex gap-2 align-items-center flex-wrap">
			@let mInfo = info();

			@if (hasType()) {
				<div class="flex flex-column align-items-center">
					<label class="text-success text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Type</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.frameType }}</span>
				</div>
			}
			@if (hasExposure() && mInfo.exposureTime) {
				<div
					class="flex flex-column align-items-center"
					pTooltip="{{ mInfo.exposureTime }} µs"
					tooltipPosition="bottom"
					[life]="2500">
					<label class="text-orange-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Exposure</label>
					<span class="min-w-4rem text-center text-sm">{{ mInfo.exposureAmount || '∞' }} / {{ mInfo.exposureTime | exposureTime }}</span>
				</div>
			}
			@if (mInfo.exposureDelay) {
				<div class="flex flex-column align-items-center">
					<label class="text-info text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Delay</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.exposureDelay * 1000000 | exposureTime }}</span>
				</div>
			}
			@if (mInfo.x !== undefined && mInfo.y !== undefined && mInfo.width && mInfo.height) {
				<div class="flex flex-column align-items-center">
					<label class="text-yellow-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">ROI</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.x }} {{ mInfo.y }} {{ mInfo.width }} {{ mInfo.height }}</span>
				</div>
			}
			@if (mInfo.binX && mInfo.binY) {
				<div class="flex flex-column align-items-center">
					<label class="text-cyan-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Bin</label>
					<span class="min-w-2rem text-center text-sm">{{ mInfo.binX }}x{{ mInfo.binY }}</span>
				</div>
			}
			@if (mInfo.gain) {
				<div class="flex flex-column align-items-center">
					<label class="text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Gain</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.gain }}</span>
				</div>
			}
			@if (mInfo.offset) {
				<div class="flex flex-column align-items-center">
					<label class="text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Offset</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.offset }}</span>
				</div>
			}
			@if (mInfo.frameFormat) {
				<div class="flex flex-column align-items-center">
					<label class="text-purple-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Format</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.frameFormat }}</span>
				</div>
			}
			@if (hasFilter) {
				<div class="flex flex-row gap-1 align-items-center relative">
					<div class="flex flex-column align-items-center">
						<label class="text-teal-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Filter</label>
						<span class="min-w-3rem text-center text-sm">{{ filter }}</span>
					</div>
					@if (canRemoveFilter() && !disabled()) {
						<i
							class="mdi mdi-close mdi-xs cursor-pointer absolute border-round-3xl bg-red-500 text-gray-800"
							style="padding: 1px; top: -6px; left: calc(50% + 10px);"
							(click)="filterRemoved.emit()"></i>
					}
				</div>
			}
			@if (hasFilter && focuser() && mInfo.focusOffset) {
				<div class="flex flex-column align-items-center">
					<label class="text-indigo-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Focus Offset</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.focusOffset }}</span>
				</div>
			}
			@if (rotator() && mInfo.angle >= 0) {
				<div class="flex flex-row gap-1 align-items-center relative">
					<div class="flex flex-column align-items-center">
						<label class="text-green-300 text-xs bg-black-alpha-40 p-2-4 mb-1px border-round-sm">Angle</label>
						<span class="min-w-3rem text-center text-sm">{{ mInfo.angle.toFixed(1) }}°</span>
					</div>
					@if (canRemoveAngle() && !disabled()) {
						<i
							class="mdi mdi-close mdi-xs cursor-pointer absolute border-round-3xl bg-red-500 text-gray-800"
							style="padding: 1px; top: -6px; left: calc(50% + 10px);"
							(click)="angleRemoved.emit()"></i>
					}
				</div>
			}
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class CameraInfoComponent {
	readonly info = input.required<CameraStartCapture>()
	readonly disabled = input<boolean | undefined>(false)
	readonly wheel = input<Wheel>()
	readonly focuser = input<Focuser>()
	readonly rotator = input<Rotator>()
	readonly hasType = input<boolean>(true)
	readonly hasExposure = input<boolean>(true)
	readonly canRemoveFilter = input(false)
	readonly canRemoveAngle = input(false)
	readonly filterRemoved = output()
	readonly angleRemoved = output()

	get hasFilter() {
		const wheel = this.wheel()
		return !!wheel && !!this.info().filterPosition && wheel.connected
	}

	get filter() {
		const wheel = this.wheel()
		const info = this.info()

		if (wheel && info.filterPosition) {
			return wheel.names[info.filterPosition - 1] || `#${info.filterPosition}`
		} else {
			return undefined
		}
	}
}
