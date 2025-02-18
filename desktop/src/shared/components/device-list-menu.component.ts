import { Component, ViewEncapsulation, effect, inject, input, output, viewChild } from '@angular/core'
import { SEPARATOR_MENU_ITEM } from '../constants'
import { AngularService } from '../services/angular.service'
import { ApiService } from '../services/api.service'
import { isGuideHead } from '../types/camera.types'
import type { Device } from '../types/device.types'
import { deviceComparator } from '../utils/comparators'
import type { Undefinable } from '../utils/types'
import type { DialogMenuComponent } from './dialog-menu.component'
import type { MenuItem, SlideMenuItem } from './menu-item.component'

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
	private readonly api = inject(ApiService)
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
					slideMenu: [],
					toolbarMenu: [
						...toolbarMenu,
						{
							icon: 'mdi ' + (device.connected ? 'mdi-close' : 'mdi-connection'),
							severity: device.connected ? 'danger' : 'info',
							label: device.connected ? 'Disconnect' : 'Connect',
							visible: !isGuideHead(device),
							command: async (event) => {
								if (event.item) {
									const connectionEvent = await DeviceListMenuComponent.handleConnectDevice(this.api, device, event.item)
									if (connectionEvent) this.deviceConnect.emit(connectionEvent)
								}
							},
						},
					],
					command: () => {
						if (!this.disableIfDeviceIsNotConnected() || device.connected) {
							resolve(device)
						} else {
							this.angularService.message('Please connect the device first', 'warn')
						}
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

	static async handleConnectDevice(api: ApiService, device: Device, item: MenuItem) {
		const connect = !device.connected

		if (connect) await api.indiDeviceConnect(device)
		else await api.indiDeviceDisconnect(device)

		item.disabled = true

		return new Promise<DeviceConnectionCommandEvent | undefined>((resolve) => {
			let counter = 0

			const timer = setInterval(async () => {
				Object.assign(device, await api.indiDevice(device))

				if (connect === device.connected) {
					if (connect && device.connected) {
						item.icon = 'mdi mdi-close'
						item.severity = 'danger'
						item.label = 'Disconnect'
					} else if (!connect && !device.connected) {
						item.icon = 'mdi mdi-connection'
						item.severity = 'info'
						item.label = 'Connect'
					}

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
