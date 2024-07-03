import { AfterContentInit, Component, NgZone, ViewChild } from '@angular/core'
import { dirname } from 'path'
import { DeviceChooserComponent } from '../../shared/components/device-chooser/device-chooser.component'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { MenuItem, SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera } from '../../shared/types/camera.types'
import { Device } from '../../shared/types/device.types'
import { Focuser } from '../../shared/types/focuser.types'
import { CONNECTION_TYPES, ConnectionDetails, EMPTY_CONNECTION_DETAILS, HomeWindowType } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { Undefinable } from '../../shared/utils/types'
import { AppComponent } from '../app.component'

interface MappedDevice {
	CAMERA: Camera
	MOUNT: Mount
	FOCUSER: Focuser
	WHEEL: FilterWheel
	ROTATOR: Rotator
}

function scrollPageOf(element: Element) {
	return parseInt(element.getAttribute('scroll-page') ?? '0')
}

@Component({
	selector: 'neb-home',
	templateUrl: './home.component.html',
	styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements AfterContentInit {
	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('imageMenu')
	private readonly imageMenu!: DeviceListMenuComponent

	readonly connectionTypes = Array.from(CONNECTION_TYPES)
	showConnectionDialog = false
	connections: ConnectionDetails[] = []
	connection?: ConnectionDetails
	newConnection?: [ConnectionDetails, Undefinable<ConnectionDetails>]

	cameras: Camera[] = []
	mounts: Mount[] = []
	focusers: Focuser[] = []
	wheels: FilterWheel[] = []
	rotators: Rotator[] = []
	domes: Camera[] = []
	switches: Camera[] = []

	currentPage = 0

	get connected() {
		return !!this.connection && this.connection.connected
	}

	get hasCamera() {
		return this.cameras.length > 0
	}

	get hasMount() {
		return this.mounts.length > 0
	}

	get hasFocuser() {
		return this.focusers.length > 0
	}

	get hasWheel() {
		return this.wheels.length > 0
	}

	get hasDome() {
		return this.domes.length > 0
	}

	get hasSwitch() {
		return this.switches.length > 0
	}

	get hasRotator() {
		return this.rotators.length > 0
	}

	get hasGuider() {
		return this.hasCamera && this.hasMount
	}

	get hasAlignment() {
		return this.hasCamera && this.hasMount
	}

	get hasSequencer() {
		return this.hasCamera
	}

	get hasAutoFocus() {
		return this.hasCamera && this.hasFocuser
	}

	get hasFlatWizard() {
		return this.hasCamera
	}

	get hasDevices() {
		return this.hasCamera || this.hasMount || this.hasFocuser || this.hasWheel || this.hasDome || this.hasRotator || this.hasSwitch
	}

	get hasINDI() {
		return this.connection?.type === 'INDI' && this.hasDevices
	}

	get hasAlpaca() {
		return this.connection?.type === 'ALPACA' && this.hasDevices
	}

	readonly deviceModel: MenuItem[] = []

	readonly imageModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-image-plus',
			label: 'Open new image',
			slideMenu: [],
			command: () => {
				return this.openImage(true)
			},
		},
	]

	private startListening<K extends keyof MappedDevice>(type: K, onAdd: (device: MappedDevice[K]) => number, onRemove: (device: MappedDevice[K]) => number, onUpdate: (device: MappedDevice[K]) => void) {
		this.electron.on(`${type}.ATTACHED`, (event) => {
			this.ngZone.run(() => {
				onAdd(event.device as never)
			})
		})

		this.electron.on(`${type}.DETACHED`, (event) => {
			this.ngZone.run(() => {
				onRemove(event.device as never)
			})
		})

		this.electron.on(`${type}.UPDATED`, (event) => {
			this.ngZone.run(() => {
				onUpdate(event.device as never)
			})
		})
	}

	constructor(
		app: AppComponent,
		private readonly electron: ElectronService,
		private readonly browserWindow: BrowserWindowService,
		private readonly api: ApiService,
		private readonly prime: PrimeService,
		private readonly preference: PreferenceService,
		private readonly ngZone: NgZone,
	) {
		app.title = 'Nebulosa'

		this.startListening(
			'CAMERA',
			(device) => {
				return this.cameras.push(device)
			},
			(device) => {
				const found = this.cameras.findIndex((e) => e.id === device.id)
				this.cameras.splice(found, 1)
				return this.cameras.length
			},
			(device) => {
				const found = this.cameras.find((e) => e.id === device.id)
				if (!found) return
				Object.assign(found, device)
			},
		)

		this.startListening(
			'MOUNT',
			(device) => {
				return this.mounts.push(device)
			},
			(device) => {
				const found = this.mounts.findIndex((e) => e.id === device.id)
				this.mounts.splice(found, 1)
				return this.mounts.length
			},
			(device) => {
				const found = this.mounts.find((e) => e.id === device.id)
				if (!found) return
				Object.assign(found, device)
			},
		)

		this.startListening(
			'FOCUSER',
			(device) => {
				return this.focusers.push(device)
			},
			(device) => {
				const found = this.focusers.findIndex((e) => e.id === device.id)
				this.focusers.splice(found, 1)
				return this.focusers.length
			},
			(device) => {
				const found = this.focusers.find((e) => e.id === device.id)
				if (!found) return
				Object.assign(found, device)
			},
		)

		this.startListening(
			'WHEEL',
			(device) => {
				return this.wheels.push(device)
			},
			(device) => {
				const found = this.wheels.findIndex((e) => e.id === device.id)
				this.wheels.splice(found, 1)
				return this.wheels.length
			},
			(device) => {
				const found = this.wheels.find((e) => e.id === device.id)
				if (!found) return
				Object.assign(found, device)
			},
		)

		this.startListening(
			'ROTATOR',
			(device) => {
				return this.rotators.push(device)
			},
			(device) => {
				const found = this.rotators.findIndex((e) => e.id === device.id)
				this.rotators.splice(found, 1)
				return this.rotators.length
			},
			(device) => {
				const found = this.rotators.find((e) => e.id === device.id)
				if (!found) return
				Object.assign(found, device)
			},
		)

		electron.on('CONNECTION.CLOSED', async (event) => {
			if (this.connection?.id === event.id) {
				await ngZone.run(() => {
					return this.updateConnection()
				})
			}
		})

		this.connections = preference.connections.get().sort((a, b) => (b.connectedAt ?? 0) - (a.connectedAt ?? 0))
		this.connections.forEach((e) => {
			e.id = undefined
			e.connected = false
		})
		this.connection = this.connections[0]
	}

	async ngAfterContentInit() {
		await this.updateConnection()

		if (this.connected) {
			this.cameras = await this.api.cameras()
			this.mounts = await this.api.mounts()
			this.focusers = await this.api.focusers()
			this.wheels = await this.api.wheels()
			this.rotators = await this.api.rotators()
		}
	}

	addConnection() {
		this.newConnection = [structuredClone(EMPTY_CONNECTION_DETAILS), undefined]
		this.showConnectionDialog = true
	}

	editConnection(connection: ConnectionDetails, event: MouseEvent) {
		this.newConnection = [structuredClone(connection), connection]
		this.showConnectionDialog = true
		event.stopImmediatePropagation()
	}

	deleteConnection(connection: ConnectionDetails, event: MouseEvent) {
		const index = this.connections.findIndex((e) => e === connection)

		if (index >= 0 && !connection.connected) {
			this.connections.splice(index, 1)

			if (connection === this.connection) {
				this.connection = this.connections[0]
			}

			this.preference.connections.set(this.connections)
		}

		event.stopImmediatePropagation()
	}

	saveConnection() {
		if (this.newConnection) {
			// Edit.
			if (this.newConnection[1]) {
				Object.assign(this.newConnection[1], this.newConnection[0])
			}
			// New.
			else {
				const newConnection = structuredClone(this.newConnection[0])
				this.connections = [...this.connections, newConnection]
				this.connection = newConnection
			}
		}

		this.preference.connections.set(this.connections)

		this.newConnection = undefined
		this.showConnectionDialog = false
	}

	async connect() {
		try {
			if (this.connection && !this.connection.connected) {
				this.connection.id = await this.api.connect(this.connection.host, this.connection.port, this.connection.type)
			}
		} catch (e) {
			console.error(e)

			this.prime.message('Connection failed', 'error')
		} finally {
			await this.updateConnection()
		}
	}

	async disconnect() {
		try {
			if (this.connection?.id && this.connection.connected) {
				await this.api.disconnect(this.connection.id)
			}
		} catch (e) {
			console.error(e)
		} finally {
			await this.updateConnection()
		}
	}

	protected findDeviceById(id: string) {
		return this.cameras.find((e) => e.id === id) ?? this.mounts.find((e) => e.id === id) ?? this.wheels.find((e) => e.id === id) ?? this.focusers.find((e) => e.id === id) ?? this.rotators.find((e) => e.id === id)
	}

	protected deviceConnected(event: DeviceConnectionCommandEvent) {
		return DeviceChooserComponent.handleConnectDevice(this.api, event.device, event.item)
	}

	protected deviceDisconnected(event: DeviceConnectionCommandEvent) {
		return DeviceChooserComponent.handleDisconnectDevice(this.api, event.device, event.item)
	}

	private async openDevice<K extends keyof MappedDevice>(type: K) {
		this.deviceModel.length = 0

		const devices: Device[] =
			type === 'CAMERA' ? this.cameras
			: type === 'MOUNT' ? this.mounts
			: type === 'FOCUSER' ? this.focusers
			: type === 'WHEEL' ? this.wheels
			: type === 'ROTATOR' ? this.rotators
			: []

		if (devices.length === 0) return

		this.deviceMenu.header = type
		const device = await this.deviceMenu.show(devices)

		if (device && device !== 'NONE') {
			await this.openDeviceWindow(type, device as never)
		}
	}

	private async openDeviceWindow<K extends keyof MappedDevice>(type: K, device: MappedDevice[K]) {
		switch (type) {
			case 'MOUNT':
				await this.browserWindow.openMount(device as Mount, { bringToFront: true })
				break
			case 'CAMERA':
				await this.browserWindow.openCamera(device as Camera, { bringToFront: true })
				break
			case 'FOCUSER':
				await this.browserWindow.openFocuser(device as Focuser, { bringToFront: true })
				break
			case 'WHEEL':
				await this.browserWindow.openWheel(device as FilterWheel, { bringToFront: true })
				break
			case 'ROTATOR':
				await this.browserWindow.openRotator(device as Rotator, { bringToFront: true })
				break
		}
	}

	private async openImage(force: boolean = false) {
		if (force || this.cameras.length === 0) {
			const preference = this.preference.homePreference.get()
			const path = await this.electron.openImage({ defaultPath: preference.imagePath })

			if (path) {
				preference.imagePath = dirname(path)
				this.preference.homePreference.set(preference)
				await this.browserWindow.openImage({ path, source: 'PATH' })
			}
		} else {
			const camera = await this.imageMenu.show(this.cameras)

			if (camera && camera !== 'NONE') {
				await this.browserWindow.openCameraImage(camera)
			}
		}
	}

	async open(type: HomeWindowType) {
		switch (type) {
			case 'MOUNT':
			case 'CAMERA':
			case 'FOCUSER':
			case 'WHEEL':
			case 'ROTATOR':
				await this.openDevice(type)
				break
			case 'GUIDER':
				await this.browserWindow.openGuider({ bringToFront: true })
				break
			case 'SKY_ATLAS':
				await this.browserWindow.openSkyAtlas(undefined, { bringToFront: true })
				break
			case 'FRAMING':
				await this.browserWindow.openFraming(undefined, { bringToFront: true })
				break
			case 'ALIGNMENT':
				await this.browserWindow.openAlignment({ bringToFront: true })
				break
			case 'SEQUENCER':
				await this.browserWindow.openSequencer({ bringToFront: true })
				break
			case 'AUTO_FOCUS':
				await this.browserWindow.openAutoFocus({ bringToFront: true })
				break
			case 'FLAT_WIZARD':
				await this.browserWindow.openFlatWizard({ bringToFront: true })
				break
			case 'INDI':
				await this.browserWindow.openINDI(undefined, { bringToFront: true })
				break
			case 'IMAGE':
				await this.openImage()
				break
			case 'SETTINGS':
				await this.browserWindow.openSettings()
				break
			case 'CALCULATOR':
				await this.browserWindow.openCalculator()
				break
			case 'ABOUT':
				await this.browserWindow.openAbout()
				break
		}
	}

	private async updateConnection() {
		if (this.connection?.id) {
			try {
				const status = await this.api.connectionStatus(this.connection.id)

				if (status && !this.connection.connected) {
					this.connection.connectedAt = Date.now()
					this.preference.connections.set(this.connections)
					this.connection.connected = true
				} else if (!status) {
					this.connection.connected = false
				}
			} catch {
				this.connection.connected = false
			}
		} else {
			const statuses = await this.api.connectionStatuses()

			for (const status of statuses) {
				for (const connection of this.connections) {
					if (!connection.connected && (status.host === connection.host || status.ip === connection.host) && status.port === connection.port) {
						connection.id = status.id
						connection.type = status.type
						connection.connected = true
						this.connection = connection
						break
					}
				}

				if (this.connection?.connected) {
					break
				}
			}
		}

		if (!this.connection?.connected) {
			this.cameras = []
			this.mounts = []
			this.focusers = []
			this.wheels = []
			this.domes = []
			this.rotators = []
			this.switches = []
		}
	}

	scrolled(event: Event) {
		function isVisible(element: Element) {
			const bound = element.getBoundingClientRect()

			return bound.top >= 0 && bound.left >= 0 && bound.bottom <= (window.innerHeight || document.documentElement.clientHeight) && bound.right <= (window.innerWidth || document.documentElement.clientWidth)
		}

		let page = 0
		const scrollChidren = document.getElementsByClassName('scroll-child')

		for (let i = 0; i < scrollChidren.length; i++) {
			const child = scrollChidren[i]

			if (isVisible(child)) {
				page = Math.max(page, scrollPageOf(child))
			}
		}

		this.currentPage = page
	}

	scrollTo(event: Event, page: number) {
		this.currentPage = page
		this.scrollToPage(page)
		event.stopImmediatePropagation()
	}

	scrollToPage(page: number) {
		const scrollChidren = document.getElementsByClassName('scroll-child')

		for (let i = 0; i < scrollChidren.length; i++) {
			const child = scrollChidren[i]

			if (scrollPageOf(child) === page) {
				child.scrollIntoView({ behavior: 'smooth' })
				break
			}
		}
	}
}
