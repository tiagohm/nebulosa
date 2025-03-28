import { Component, ViewEncapsulation, input, output } from '@angular/core'
import type { CameraStartCapture } from '../types/camera.types'
import type { Focuser } from '../types/focuser.types'
import type { Rotator } from '../types/rotator.types'
import type { Wheel } from '../types/wheel.types'

@Component({
	standalone: false,
	selector: 'neb-camera-info',
	template: `
		<div class="flex flex-wrap items-center gap-2">
			@let mInfo = info();

			@if (hasType()) {
				<div class="flex flex-col items-center">
					<label class="text-xs text-green-500">Type</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.frameType }}</span>
				</div>
			}
			@if (hasExposure() && mInfo.exposureTime) {
				<div
					class="flex flex-col items-center"
					pTooltip="{{ mInfo.exposureTime }} µs"
					tooltipPosition="bottom"
					[life]="2500">
					<label class="text-xs text-orange-300">Exposure</label>
					<span class="min-w-4rem text-center text-sm">{{ mInfo.exposureAmount || '∞' }} / {{ mInfo.exposureTime | exposureTime }}</span>
				</div>
			}
			@if (mInfo.exposureDelay) {
				<div class="flex flex-col items-center">
					<label class="text-xs text-blue-500">Delay</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.exposureDelay * 1000000 | exposureTime }}</span>
				</div>
			}
			@if (mInfo.x !== undefined && mInfo.y !== undefined && mInfo.width && mInfo.height) {
				<div class="flex flex-col items-center">
					<label class="text-xs text-yellow-300">ROI</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.x }} {{ mInfo.y }} {{ mInfo.width }} {{ mInfo.height }}</span>
				</div>
			}
			@if (mInfo.binX && mInfo.binY) {
				<div class="flex flex-col items-center">
					<label class="text-xs text-cyan-300">Bin</label>
					<span class="min-w-2rem text-center text-sm">{{ mInfo.binX }}x{{ mInfo.binY }}</span>
				</div>
			}
			@if (mInfo.gain) {
				<div class="flex flex-col items-center">
					<label class="text-xs">Gain</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.gain }}</span>
				</div>
			}
			@if (mInfo.offset) {
				<div class="flex flex-col items-center">
					<label class="text-xs">Offset</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.offset }}</span>
				</div>
			}
			@if (mInfo.frameFormat) {
				<div class="flex flex-col items-center">
					<label class="text-xs text-purple-300">Format</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.frameFormat }}</span>
				</div>
			}
			@if (hasFilter) {
				<div
					class="flex cursor-pointer flex-col items-center"
					(click)="canRemoveFilter() && !disabled() && filterPopover.toggle($event)">
					<label class="cursor-pointer text-xs text-teal-300">Filter</label>
					<span class="min-w-3rem text-center text-sm">{{ filter }}</span>
				</div>
				<p-popover #filterPopover>
					<neb-button
						severity="danger"
						icon="mdi mdi-close"
						label="Remove"
						(action)="filterRemoved.emit()" />
				</p-popover>
			}
			@if (hasFilter && focuser() && mInfo.focusOffset) {
				<div class="flex cursor-pointer flex-col items-center">
					<label class="cursor-pointer text-xs text-indigo-300">Focus Offset</label>
					<span class="min-w-5rem text-center text-sm">{{ mInfo.focusOffset }}</span>
				</div>
			}
			@if (rotator() && mInfo.angle >= 0) {
				<div
					class="flex cursor-pointer flex-col items-center"
					(click)="canRemoveAngle() && !disabled() && anglePopover.toggle($event)">
					<label class="cursor-pointer text-xs text-green-300">Angle</label>
					<span class="min-w-3rem text-center text-sm">{{ mInfo.angle.toFixed(1) }}°</span>
				</div>
				<p-popover #anglePopover>
					<neb-button
						severity="danger"
						icon="mdi mdi-close"
						label="Remove"
						(action)="angleRemoved.emit()" />
				</p-popover>
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
