import { Component, ViewEncapsulation, input } from '@angular/core'
import type { Device } from '../types/device.types'

@Component({
	selector: 'neb-device-name',
	template: `
		<div class="flex flex-column justify-content-center gap-0">
			<span class="font-bold">{{ device().name }}</span>
			<div class="flex flex-row align-items-center text-xs text-gray-400 gap-2">
				<span>DRIVER: {{ device().driverName }}</span>
				<span>VERSION: {{ device().driverVersion }}</span>
			</div>
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DeviceNameComponent {
	readonly device = input.required<Device>()
}
