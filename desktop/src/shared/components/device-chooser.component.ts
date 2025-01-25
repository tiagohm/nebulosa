import { Component, ViewEncapsulation, inject, input, model, output, viewChild } from '@angular/core'
import { ApiService } from '../services/api.service'
import type { Device } from '../types/device.types'
import type { DeviceConnectionCommandEvent, DeviceListMenuComponent } from './device-list-menu.component'
import type { MenuItem } from './menu-item.component'

@Component({
	standalone: false,
	selector: 'neb-device-chooser',
	template: `
		<neb-button
			(action)="show()"
			[rounded]="false"
			[severity]="device()?.connected ? 'success' : 'danger'"
			[disabled]="disabled()">
			<div class="flex items-center gap-1">
				<i [class]="icon()"></i>
				<div class="flex flex-col gap-[1px] text-left">
					@let mDevice = device();

					<span class="text-sm font-bold">{{ title() }}</span>
					@if (mDevice && mDevice.id) {
						<span class="text-xs font-normal">{{ mDevice.name }}</span>
					} @else {
						<span class="text-xs font-normal">{{ noDeviceMessage() || 'Choose a device' }}</span>
					}
				</div>
			</div>
		</neb-button>

		<neb-device-list-menu
			#deviceMenu
			[hasNone]="hasNone()"
			[disableIfDeviceIsNotConnected]="false"
			[header]="title()"
			(deviceConnect)="deviceConnected($event)"
			(deviceDisconnect)="deviceDisconnected($event)" />
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DeviceChooserComponent<T extends Device = Device> {
	private readonly api = inject(ApiService)
	readonly title = input.required<string>()
	readonly noDeviceMessage = input<string>()
	readonly icon = input.required<string>()
	readonly devices = input.required<T[]>()
	readonly hasNone = input<boolean>(false)
	readonly device = model<T>()
	readonly disabled = input<boolean>()
	readonly deviceConnect = output<DeviceConnectionCommandEvent>()
	readonly deviceDisconnect = output<DeviceConnectionCommandEvent>()

	private readonly deviceMenu = viewChild.required<DeviceListMenuComponent>('deviceMenu')

	async show() {
		const device = await this.deviceMenu().show(this.devices(), this.device())

		if (device) {
			this.device.set(device === 'NONE' ? undefined : device)
		}
	}

	hide() {
		this.deviceMenu().hide()
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

		return new Promise<DeviceConnectionCommandEvent | undefined>((resolve) => {
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
