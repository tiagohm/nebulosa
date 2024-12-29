import { Component, ViewEncapsulation, input } from '@angular/core'
import type { Device } from '../types/device.types'

@Component({
	standalone: false,
	selector: 'neb-device-name',
	template: `
		<div class="flex flex-col justify-center gap-0">
			@let mDevice = device();

			<span class="white-space-nowrap font-bold">{{ mDevice.name }}</span>

			@if (mDevice.driver.name) {
				<div class="flex flex-row items-center gap-1 text-xs text-gray-400">
					<span>DRIVER: {{ mDevice.driver.name }}</span>
					<span>V{{ mDevice.driver.version }}</span>
				</div>
			}
		</div>
	`,
	host: {
		'[class.text-overflow-scroll]': 'true',
	},
	encapsulation: ViewEncapsulation.None,
})
export class DeviceNameComponent {
	readonly device = input.required<Device>()
}
