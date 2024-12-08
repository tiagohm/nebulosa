import { inject, Injectable } from '@angular/core'
import { DeviceListMenuComponent } from '../components/device-list-menu.component'
import { Device } from '../types/device.types'
import { AngularService } from './angular.service'

@Injectable({ providedIn: 'root' })
export class DeviceService {
	private readonly angularService = inject(AngularService)

	async executeAction<T extends Device>(deviceMenu: DeviceListMenuComponent, devices: T[], action: (device: T) => void | Promise<void>, showConfirmation: boolean = true) {
		if (showConfirmation && (await this.angularService.confirm('Are you sure that you want to proceed?'))) {
			return false
		}

		if (!devices.length) {
			this.angularService.message('No equipment available to perform this action!', 'warn')
			return false
		} else {
			const device = devices.length === 1 ? devices[0] : await deviceMenu.show(devices, undefined, devices[0].type)

			if (device && device !== 'NONE') {
				if (device.connected) {
					await action(device)
					return true
				} else {
					this.angularService.message('Your equipment must be connected to perform this action', 'error')
				}
			}
		}

		return false
	}
}
