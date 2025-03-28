import { Component, ViewEncapsulation, input, model, viewChild } from '@angular/core'
import type { Device } from '../types/device.types'
import type { DeviceListMenuComponent } from './device-list-menu.component'

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
			[header]="title()" />
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DeviceChooserComponent<T extends Device = Device> {
	readonly title = input.required<string>()
	readonly noDeviceMessage = input<string>()
	readonly icon = input.required<string>()
	readonly devices = input.required<T[]>()
	readonly hasNone = input<boolean>(false)
	readonly device = model<T>()
	readonly disabled = input<boolean>()

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
}
