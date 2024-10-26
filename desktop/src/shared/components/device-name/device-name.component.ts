import { Component, Input, ViewEncapsulation } from '@angular/core'
import type { Device } from '../../types/device.types'

@Component({
	selector: 'neb-device-name',
	template: `
		<div class="flex flex-col justify-center gap-0">
			<span class="font-bold">{{ device.name }}</span>
			<div class="flex flex-row items-center gap-2 text-xs text-gray-400">
				<span>DRIVER: {{ device.driverName }}</span>
				<span>VERSION: {{ device.driverVersion }}</span>
			</div>
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DeviceNameComponent {
	@Input({ required: true })
	readonly device!: Device
}
