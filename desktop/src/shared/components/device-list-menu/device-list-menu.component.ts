import { Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation } from '@angular/core'
import { SEPARATOR_MENU_ITEM } from '../../constants'
import { AngularService } from '../../services/angular.service'
import { isGuideHead } from '../../types/camera.types'
import { Device } from '../../types/device.types'
import { deviceComparator } from '../../utils/comparators'
import { Undefinable } from '../../utils/types'
import { DialogMenuComponent } from '../dialog-menu/dialog-menu.component'
import { MenuItem, SlideMenuItem } from '../menu-item/menu-item.component'

export interface DeviceConnectionCommandEvent {
	device: Device
	item: MenuItem
}

@Component({
	selector: 'neb-device-list-menu',
	templateUrl: './device-list-menu.component.html',
	styleUrls: ['./device-list-menu.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class DeviceListMenuComponent {
	@Input()
	protected readonly model: SlideMenuItem[] = []

	@Input()
	protected readonly modelAtFirst: boolean = true

	@Input()
	protected readonly disableIfDeviceIsNotConnected: boolean = true

	@Input()
	protected header?: string

	@Input()
	protected readonly hasNone: boolean = false

	@Input()
	protected readonly toolbarBuilder?: (device: Device) => MenuItem[]

	@Output()
	readonly deviceConnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@Output()
	readonly deviceDisconnect = new EventEmitter<DeviceConnectionCommandEvent>()

	@ViewChild('menu')
	private readonly menu!: DialogMenuComponent

	constructor(private readonly angularService: AngularService) {}

	show<T extends Device>(devices: T[], selected?: NoInfer<T>, header?: string) {
		const model: SlideMenuItem[] = []

		if (header) this.header = header

		return new Promise<Undefinable<T | 'NONE'>>((resolve) => {
			if (devices.length <= 0) {
				resolve(undefined)
				this.angularService.message('Please connect your equipment first!', 'warning')
				return
			}

			const populateWithModel = () => {
				for (const item of this.model) {
					model.push({
						...item,
						command: (event) => {
							item.command?.(event)
							resolve(undefined)
						},
					})
				}
			}

			const subscription = this.menu.visibleChange.subscribe((visible) => {
				if (!visible) {
					subscription.unsubscribe()
					resolve(undefined)
				}
			})

			if (this.model.length > 0 && this.modelAtFirst) {
				populateWithModel()
				model.push(SEPARATOR_MENU_ITEM)
			}

			if (this.hasNone) {
				model.push({
					icon: 'mdi mdi-close',
					label: 'None',
					selected: !selected,
					slideMenu: [],
					command: () => {
						resolve('NONE')
					},
				})
			}

			for (const device of devices.sort(deviceComparator)) {
				const toolbarMenu = this.toolbarBuilder?.(device) ?? []

				model.push({
					label: device.name,
					selected: selected === device,
					disabled: this.disableIfDeviceIsNotConnected && !device.connected,
					slideMenu: [],
					toolbarMenu: [
						...toolbarMenu,
						{
							icon: 'mdi ' + (device.connected ? 'mdi-close' : 'mdi-connection'),
							severity: device.connected ? 'danger' : 'info',
							label: device.connected ? 'Disconnect' : 'Connect',
							visible: !isGuideHead(device),
							command: (event) => {
								if (event.item) {
									if (device.connected) this.deviceDisconnect.emit({ device, item: event.item })
									else this.deviceConnect.emit({ device, item: event.item })
								}
							},
						},
					],
					command: () => {
						resolve(device)
					},
				})
			}

			if (this.model.length > 0 && !this.modelAtFirst) {
				model.push(SEPARATOR_MENU_ITEM)
				populateWithModel()
			}

			this.menu.show(model)
		})
	}

	hide() {
		this.menu.hide()
	}
}
