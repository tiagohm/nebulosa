import { Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation } from '@angular/core'
import { ApiService } from '../../services/api.service'
import { Device } from '../../types/device.types'
import { Undefinable } from '../../utils/types'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../device-list-menu/device-list-menu.component'
import { MenuItem } from '../menu-item/menu-item.component'

@Component({
	selector: 'neb-device-chooser',
	templateUrl: './device-chooser.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class DeviceChooserComponent<T extends Device = Device> {
	@Input({ required: true })
	title!: string

	@Input()
	noDeviceMessage?: string

	@Input({ required: true })
	icon!: string

	@Input({ required: true })
	devices!: T[]

	@Input()
	hasNone: boolean = false

	@Input()
	device?: T

	@Input()
	disabled?: boolean

	@Output()
	deviceChange = new EventEmitter<T>()

	@Output()
	deviceConnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@Output()
	deviceDisconnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	constructor(private readonly api: ApiService) {}

	async show() {
		const device = await this.deviceMenu.show(this.devices, this.device)

		if (device) {
			this.device = device === 'NONE' ? undefined : device
			this.deviceChange.emit(this.device)
		}
	}

	hide() {
		this.deviceMenu.hide()
	}

	async deviceConnected(event: DeviceConnectionCommandEvent) {
		const newEvent = await DeviceChooserComponent.handleConnectDevice(this.api, event.device, event.item)
		if (newEvent) this.deviceConnect.emit(newEvent)
	}

	async deviceDisconnected(event: DeviceConnectionCommandEvent) {
		const newEvent = await DeviceChooserComponent.handleDisconnectDevice(this.api, event.device, event.item)
		if (newEvent) this.deviceDisconnect.emit(newEvent)
	}

	static async handleConnectDevice(api: ApiService, device: Device, item: MenuItem) {
		if (device.connected) return undefined

		await api.indiDeviceConnect(device)

		item.disabled = true

		return new Promise<DeviceConnectionCommandEvent | undefined>((resolve) => {
			let counter = 0

			const timer = setInterval(async () => {
				Object.assign(device, await api.indiDevice(device))

				if (device.connected) {
					item.icon = 'mdi mdi-close'
					item.severity = 'danger'
					item.label = 'Disconnect'
					clearInterval(timer)
					resolve({ device, item })
				} else if (counter >= 10) {
					clearInterval(timer)
					resolve(undefined)
				} else {
					counter++
					return
				}

				item.disabled = false
			}, 1500)
		})
	}

	static async handleDisconnectDevice(api: ApiService, device: Device, item: MenuItem) {
		if (!device.connected) return undefined

		await api.indiDeviceDisconnect(device)

		item.disabled = true

		return new Promise<Undefinable<DeviceConnectionCommandEvent>>((resolve) => {
			let counter = 0

			const timer = setTimeout(async () => {
				Object.assign(device, await api.indiDevice(device))

				if (!device.connected) {
					item.icon = 'mdi mdi-connection'
					item.severity = 'info'
					item.label = 'Connect'
					clearInterval(timer)
					resolve({ device, item })
				} else if (counter >= 10) {
					clearInterval(timer)
					resolve(undefined)
				} else {
					counter++
					return
				}

				item.disabled = false
			}, 1500)
		})
	}
}
