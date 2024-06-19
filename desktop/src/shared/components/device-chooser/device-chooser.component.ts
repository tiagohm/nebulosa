import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { ApiService } from '../../services/api.service'
import { Device } from '../../types/device.types'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../device-list-menu/device-list-menu.component'
import { MenuItem } from '../menu-item/menu-item.component'

@Component({
	selector: 'neb-device-chooser',
	templateUrl: './device-chooser.component.html',
	styleUrls: ['./device-chooser.component.scss'],
})
export class DeviceChooserComponent<T extends Device = Device> {
	@Input({ required: true })
	readonly title!: string

	@Input()
	readonly noDeviceMessage?: string

	@Input({ required: true })
	readonly icon!: string

	@Input({ required: true })
	readonly devices!: T[]

	@Input()
	readonly hasNone: boolean = false

	@Input()
	device?: T

	@Output()
	readonly deviceChange = new EventEmitter<T>()

	@Output()
	readonly deviceConnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@Output()
	readonly deviceDisconnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	constructor(private api: ApiService) {}

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

	protected async deviceConnected(event: DeviceConnectionCommandEvent) {
		const newEvent = await DeviceChooserComponent.handleConnectDevice(this.api, event.device, event.item)
		if (newEvent) this.deviceConnect.emit(newEvent)
	}

	protected async deviceDisconnected(event: DeviceConnectionCommandEvent) {
		const newEvent = await DeviceChooserComponent.handleDisconnectDevice(this.api, event.device, event.item)
		if (newEvent) this.deviceDisconnect.emit(newEvent)
	}

	static async handleConnectDevice(api: ApiService, device: Device, item: MenuItem) {
		await api.indiDeviceConnect(device)

		item.disabled = true

		return new Promise<DeviceConnectionCommandEvent | undefined>((resolve) => {
			setTimeout(async () => {
				Object.assign(device, await api.indiDevice(device))

				if (device.connected) {
					item.icon = 'mdi mdi-close'
					item.severity = 'danger'
					item.label = 'Disconnect'
					resolve({ device, item })
				} else {
					resolve(undefined)
				}

				item.disabled = false
			}, 1000)
		})
	}

	static async handleDisconnectDevice(api: ApiService, device: Device, item: MenuItem) {
		await api.indiDeviceDisconnect(device)

		item.disabled = true

		return new Promise<DeviceConnectionCommandEvent | undefined>((resolve) => {
			setTimeout(async () => {
				Object.assign(device, await api.indiDevice(device))

				if (!device.connected) {
					item.icon = 'mdi mdi-connection'
					item.severity = 'info'
					item.label = 'Connect'
					resolve({ device, item })
				} else {
					resolve(undefined)
				}

				item.disabled = false
			}, 1000)
		})
	}
}
