import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { Listbox } from 'primeng/listbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Device, INDIProperty, INDIPropertyItem, INDISendProperty } from '../../shared/types/device.types'
import { deviceComparator, textComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-indi',
	templateUrl: './indi.component.html',
	styleUrls: ['./indi.component.scss'],
})
export class INDIComponent implements AfterViewInit, OnDestroy {
	devices: Device[] = []
	properties: INDIProperty<any>[] = []
	groups: MenuItem[] = []

	device?: Device
	group = ''
	showLog = false
	messages: string[] = []

	@ViewChild('listbox')
	readonly messageListbox!: Listbox

	constructor(
		app: AppComponent,
		private route: ActivatedRoute,
		private api: ApiService,
		electron: ElectronService,
		ngZone: NgZone,
	) {
		app.title = 'INDI'

		electron.on('DEVICE.PROPERTY_CHANGED', (event) => {
			if (this.device?.id === event.device.id) {
				ngZone.run(() => {
					this.addOrUpdateProperty(event.property!)
					this.updateGroups()
				})
			}
		})

		electron.on('DEVICE.PROPERTY_DELETED', (event) => {
			if (this.device?.id === event.device.id) {
				const index = this.properties.findIndex((e) => e.name === event.property!.name)

				if (index >= 0) {
					ngZone.run(() => {
						this.properties.splice(index, 1)
						this.updateGroups()
					})
				}
			}
		})

		electron.on('DEVICE.MESSAGE_RECEIVED', (event) => {
			if (this.device && event.device?.id === this.device.id) {
				ngZone.run(() => {
					this.messages.splice(0, 0, event.message!)
					this.messageListbox.cd.markForCheck()
				})
			}
		})
	}

	async ngAfterViewInit() {
		this.route.queryParams.subscribe((e) => {
			const device = JSON.parse(decodeURIComponent(e.data))

			if ('id' in device && device.id) {
				this.device = device
			}
		})

		this.devices = [...(await this.api.cameras()), ...(await this.api.mounts()), ...(await this.api.focusers()), ...(await this.api.wheels())].sort(deviceComparator)

		this.device = this.devices[0]

		if (this.device) {
			this.deviceChanged(this.device)
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		if (this.device) {
			this.api.indiUnlisten(this.device)
		}
	}

	async deviceChanged(device: Device) {
		if (this.device) {
			await this.api.indiUnlisten(this.device)
		}

		this.device = device

		this.updateProperties()
		await this.api.indiListen(device)
		this.messages = await this.api.indiLog(device)
	}

	changeGroup(group: string) {
		this.showLog = false
		this.group = group
	}

	send(property: INDISendProperty) {
		this.api.indiSendProperty(this.device!, property)
	}

	private updateGroups() {
		const groups = new Set<string>()

		for (const property of this.properties) {
			groups.add(property.group)
		}

		let groupsChanged = false

		if (this.groups.length === groups.size) {
			for (const group of groups) {
				if (!this.groups.find((e) => e.label === group)) {
					groupsChanged = true
					break
				}
			}
			for (const group of this.groups) {
				if (!groups.has(group.label!)) {
					groupsChanged = true
					break
				}
			}
		} else {
			groupsChanged = true
		}

		if (this.groups.length === 0 || groupsChanged) {
			this.groups = Array.from(groups)
				.sort(textComparator)
				.map(
					(e) =>
						<MenuItem>{
							icon: 'mdi mdi-sitemap',
							label: e,
							command: () => this.changeGroup(e),
						},
				)
		}

		if (!this.group || !this.groups.find((e) => e.label === this.group)) {
			this.group = this.groups[0].label!
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
		}
	}

	private addOrUpdateProperty<T>(property: INDIProperty<T>) {
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
