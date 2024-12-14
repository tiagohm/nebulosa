import { Component, ViewEncapsulation, input } from '@angular/core'
import type { Device } from '../types/device.types'

@Component({
	selector: 'neb-device-name',
	template: `
		<div class="flex flex-column justify-content-center gap-0">
			@let mDevice = device();

			<span class="font-bold white-space-nowrap">{{ mDevice.name }}</span>

			@if (mDevice.driver.name) {
				<div class="flex flex-row align-items-center text-xs text-gray-400 gap-1">
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
