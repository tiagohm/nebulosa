import type { OnDestroy } from '@angular/core'
import { Component, HostListener, NgZone, ViewEncapsulation, effect, inject, viewChild } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import type { Listbox } from 'primeng/listbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import type { Device, INDIProperty, INDIPropertyItem, INDISendProperty } from '../../shared/types/device.types'
import { deviceComparator, textComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-indi',
	templateUrl: 'indi.component.html',
	styleUrls: ['indi.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class INDIComponent implements OnDestroy {
	private readonly api = inject(ApiService)
	private readonly data = injectQueryParams('data', { transform: (v) => v && decodeURIComponent(v) })

	protected devices: Device[] = []
	protected properties: INDIProperty[] = []
	protected groups: string[] = []

	protected device?: Device
	protected group = ''
	protected showLog = false
	protected messages: string[] = []

	private readonly messageBox = viewChild.required<Listbox>('messageBox')

	constructor() {
		const app = inject(AppComponent)
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		app.title = 'INDI'

		electronService.on('DEVICE.PROPERTY_CHANGED', (event) => {
			if (event.device.id === this.device?.id) {
				ngZone.run(() => {
					if (event.property) {
						this.addOrUpdateProperty(event.property)
						this.updateGroups()
					}
				})
			}
		})

		electronService.on('DEVICE.PROPERTY_DELETED', (event) => {
			if (event.device.id === this.device?.id) {
				const index = this.properties.findIndex((e) => e.name === event.property?.name)

				if (index >= 0) {
					ngZone.run(() => {
						this.properties.splice(index, 1)
						this.updateGroups()
					})
				}
			}
		})

		electronService.on('DEVICE.MESSAGE_RECEIVED', (event) => {
			if (event.device.id === this.device?.id) {
				ngZone.run(() => {
					if (event.message) {
						this.messages.splice(0, 0, event.message)
						this.messageBox().cd.markForCheck()
					}
				})
			}
		})

		electronService.on('DATA.CHANGED', (device: Device) => {
			if (device.id) {
				ngZone.run(() => {
					void this.deviceChanged(device)
				})
			}
		})

		effect(async () => {
			const data = this.data()

			await this.loadDevices()

			if (data) {
				const device = JSON.parse(data) as Device

				if (device.id) {
					await this.deviceChanged(device)
				}
			}
		})
	}

	private async loadDevices() {
		const [cameras, mounts, wheels, focusers, rotators, guideOutputs, lightBoxes, dustCaps] = await Promise.all([this.api.cameras(), this.api.mounts(), this.api.wheels(), this.api.focusers(), this.api.rotators(), this.api.guideOutputs(), this.api.lightBoxes(), this.api.dustCaps()])
		const devices: Device[] = []

		devices.push(...cameras.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...mounts.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...wheels.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...focusers.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...rotators.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...guideOutputs.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...lightBoxes.filter((a) => !devices.find((b) => a.name === b.name)))
		devices.push(...dustCaps.filter((a) => !devices.find((b) => a.name === b.name)))

		this.devices = devices.sort(deviceComparator)

		if (this.devices.length) {
			await this.deviceChanged(this.devices[0])
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		if (this.device) {
			void this.api.indiUnlisten(this.device)
		}
	}

	protected async deviceChanged(device?: Device) {
		if (device) {
			if (this.device) {
				await this.api.indiUnlisten(this.device)
			}

			this.device = this.devices.find((e) => e.id === device.id || e.name === device.name)

			await this.updateProperties()

			await this.api.indiListen(device)
			this.messages = await this.api.indiMessages(device)
		} else {
			this.device = undefined
			this.properties = []
			this.groups = []
		}
	}

	protected changeGroup(group: string) {
		this.showLog = false
		this.group = group
	}

	async send(property: INDISendProperty) {
		if (this.device) {
			await this.api.indiSendProperty(this.device, property)
		}
	}

	private updateGroups() {
		const groups = new Set<string>()

		for (const property of this.properties) {
			groups.add(property.group)
		}

		let groupsChanged = false

		if (this.groups.length === groups.size) {
			for (const group of groups) {
				if (!this.groups.find((e) => e === group)) {
					groupsChanged = true
					break
				}
			}
			for (const group of this.groups) {
				if (group && !groups.has(group)) {
					groupsChanged = true
					break
				}
			}
		} else {
			groupsChanged = true
		}

		if (this.groups.length === 0 || groupsChanged) {
			this.groups = Array.from(groups).sort(textComparator)
		}

		if (!this.group || !this.groups.find((e) => e === this.group)) {
			this.group = this.groups[0]
		}
	}

	async updateProperties() {
		if (this.device) {
			const properties = await this.api.indiProperties(this.device)

			for (const property of properties) {
				this.addOrUpdateProperty(property)
			}

			for (let i = 0; i < this.properties.length; i++) {
				if (properties.findIndex((e) => e.name === this.properties[i].name) < 0) {
					this.properties.splice(i--, 1)
				}
			}

			this.updateGroups()
		} else {
			this.properties = []
			this.groups = []
			this.group = ''
		}
	}

	private addOrUpdateProperty(property: INDIProperty) {
		const index = this.properties.findIndex((e) => e.name === property.name)

		if (index >= 0) {
			this.updateProperty(this.properties[index], property)
		} else {
			this.properties.push(property)
		}
	}

	private updateProperty<T>(current: INDIProperty<T>, update: INDIProperty<T>) {
		current.state = update.state
		current.perm = update.perm

		for (const item of update.items) {
			const index = current.items.findIndex((e) => e.name == item.name)

			if (index >= 0) {
				this.updatePropertyItem(current.items[index], item)
			} else {
				current.items.push(item)
			}
		}

		for (let i = 0; i < current.items.length; i++) {
			if (update.items.findIndex((e) => e.name === current.items[i].name) < 0) {
				current.items.splice(i--, 1)
			}
		}
	}

	private updatePropertyItem<T>(current: INDIPropertyItem<T>, update: INDIPropertyItem<T>) {
		current.value = update.value
	}
}
