import { Component, ViewEncapsulation, effect, inject, input, output, viewChild } from '@angular/core'
import { SEPARATOR_MENU_ITEM } from '../constants'
import { AngularService } from '../services/angular.service'
import { isGuideHead } from '../types/camera.types'
import { Device } from '../types/device.types'
import { deviceComparator } from '../utils/comparators'
import { Undefinable } from '../utils/types'
import { DialogMenuComponent } from './dialog-menu.component'
import { MenuItem, SlideMenuItem } from './menu-item.component'

export interface DeviceConnectionCommandEvent {
	device: Device
	item: MenuItem
}

@Component({
	standalone: false,
	selector: 'neb-device-list-menu',
	template: `
		<neb-dialog-menu
			#menu
			[header]="currentHeader" />
	`,
	styles: `
		neb-device-list-menu {
			.p-menuitem-link {
				padding: 0.5rem 0.75rem;
				min-height: 43px;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DeviceListMenuComponent {
	private readonly angularService = inject(AngularService)

	readonly model = input<SlideMenuItem[]>([])
	readonly modelAtFirst = input<boolean>(true)
	readonly disableIfDeviceIsNotConnected = input<boolean>(true)
	readonly header = input<string>()
	readonly hasNone = input<boolean>(false)
	readonly toolbarBuilder = input<(device: Device) => MenuItem[]>()
	readonly deviceConnect = output<DeviceConnectionCommandEvent>()
	readonly deviceDisconnect = output<DeviceConnectionCommandEvent>()

	readonly menu = viewChild.required<DialogMenuComponent>('menu')

	protected currentHeader?: string

	constructor() {
		effect(() => {
			this.currentHeader = this.header()
		})
	}

	show<T extends Device>(devices: T[], selected?: NoInfer<T>, header?: string) {
		const model: SlideMenuItem[] = []

		this.currentHeader = header || this.header()

		return new Promise<Undefinable<T | 'NONE'>>((resolve) => {
			if (devices.length <= 0) {
				resolve(undefined)
				this.angularService.message('No equipment available to perform this action!', 'warn')
				return
			}

			const populateWithModel = () => {
				for (const item of this.model()) {
					model.push({
						...item,
						command: (event) => {
							item.command?.(event)
							resolve(undefined)
						},
					})
				}
			}

			const subscription = this.menu().visible.subscribe((visible) => {
				if (!visible) {
					subscription.unsubscribe()
					resolve(undefined)
				}
			})

			const modelAtFirst = this.modelAtFirst()
			if (this.model().length > 0 && modelAtFirst) {
				populateWithModel()
				model.push(SEPARATOR_MENU_ITEM)
			}

			if (this.hasNone()) {
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
				const toolbarMenu = this.toolbarBuilder()?.(device) ?? []

				model.push({
					label: device.name,
					selected: selected === device,
					disabled: this.disableIfDeviceIsNotConnected() && !device.connected,
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

			if (this.model().length > 0 && !modelAtFirst) {
				model.push(SEPARATOR_MENU_ITEM)
				populateWithModel()
			}

			this.menu().show(model)
		})
	}

	hide() {
		this.menu().hide()
	}
}
