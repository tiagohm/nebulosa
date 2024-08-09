import { AfterContentInit, Component, NgZone, ViewChild, ViewEncapsulation } from '@angular/core'
import { dirname } from 'path'
import { DeviceChooserComponent } from '../../shared/components/device-chooser/device-chooser.component'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { MenuItem, SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { AngularService } from '../../shared/services/angular.service'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, isCamera } from '../../shared/types/camera.types'
import { Device, DeviceType } from '../../shared/types/device.types'
import { Focuser, isFocuser } from '../../shared/types/focuser.types'
import { ConnectionDetails, DEFAULT_CONNECTION_DETAILS, DEFAULT_HOME_CONNECTION_DIALOG, DEFAULT_HOME_PREFERENCE, HomeWindowType } from '../../shared/types/home.types'
import { isMount, Mount } from '../../shared/types/mount.types'
import { isRotator, Rotator } from '../../shared/types/rotator.types'
import { isWheel, Wheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

function scrollPageOf(element: Element) {
	return parseInt(element.getAttribute('scroll-page') ?? '0')
}

@Component({
	selector: 'neb-home',
	templateUrl: './home.component.html',
	styleUrls: ['./home.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class HomeComponent implements AfterContentInit {
	protected readonly preference = structuredClone(DEFAULT_HOME_PREFERENCE)
	protected connection?: ConnectionDetails
	protected readonly connectionDialog = structuredClone(DEFAULT_HOME_CONNECTION_DIALOG)

	protected cameras: Camera[] = []
	protected mounts: Mount[] = []
	protected focusers: Focuser[] = []
	protected wheels: Wheel[] = []
	protected rotators: Rotator[] = []
	protected domes: Camera[] = []
	protected switches: Camera[] = []

	protected page = 0

	protected readonly deviceModel: MenuItem[] = []

	protected readonly imageModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-image-plus',
			label: 'Open new image',
			slideMenu: [],
			command: () => {
				return this.openImage()
			},
		},
	]

	protected readonly deviceMenuToolbarBuilder = (device: Device): MenuItem[] => {
		if (isCamera(device)) {
			return [
				{
					icon: 'mdi mdi-image',
					label: 'View Image',
					command: () => {
						return this.browserWindowService.openCameraImage(device)
					},
				},
			]
		} else {
			return []
		}
	}

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

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

	constructor(
		app: AppComponent,
		private readonly electronService: ElectronService,
		private readonly browserWindowService: BrowserWindowService,
		private readonly api: ApiService,
		private readonly angularService: AngularService,
		private readonly preferenceService: PreferenceService,
		ngZone: NgZone,
	) {
		app.title = 'Nebulosa'

		electronService.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		electronService.on(`CAMERA.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		electronService.on(`CAMERA.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		electronService.on('MOUNT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		electronService.on(`MOUNT.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		electronService.on(`MOUNT.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		electronService.on('FOCUSER.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		electronService.on(`FOCUSER.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		electronService.on(`FOCUSER.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		electronService.on('WHEEL.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		electronService.on(`WHEEL.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		electronService.on(`WHEEL.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		electronService.on('ROTATOR.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		electronService.on(`ROTATOR.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		electronService.on(`ROTATOR.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		electronService.on('CONNECTION.CLOSED', async (event) => {
			if (this.connection?.id === event.id) {
				await ngZone.run(() => {
					return this.updateConnection()
				})
			}
		})
	}

	async ngAfterContentInit() {
		this.loadPreference()

		await this.updateConnection()

		if (this.connected) {
			this.cameras = await this.api.cameras()
			this.mounts = await this.api.mounts()
			this.focusers = await this.api.focusers()
			this.wheels = await this.api.wheels()
			this.rotators = await this.api.rotators()
		}
	}

	private deviceAdded(device: Device) {
		if (isCamera(device)) {
			this.cameras.push(device)
		} else if (isMount(device)) {
			this.mounts.push(device)
		} else if (isFocuser(device)) {
			this.focusers.push(device)
		} else if (isWheel(device)) {
			this.wheels.push(device)
		} else if (isRotator(device)) {
			this.rotators.push(device)
		}
	}

	private deviceRemoved(device: Device) {
		if (isCamera(device)) {
			const found = this.cameras.findIndex((e) => e.id === device.id)
			this.cameras.splice(found, 1)
		} else if (isMount(device)) {
			const found = this.mounts.findIndex((e) => e.id === device.id)
			this.mounts.splice(found, 1)
		} else if (isFocuser(device)) {
			const found = this.focusers.findIndex((e) => e.id === device.id)
			this.focusers.splice(found, 1)
		} else if (isWheel(device)) {
			const found = this.wheels.findIndex((e) => e.id === device.id)
			this.wheels.splice(found, 1)
		} else if (isRotator(device)) {
			const found = this.rotators.findIndex((e) => e.id === device.id)
			this.rotators.splice(found, 1)
		}
	}

	private deviceUpdated(device: Device) {
		if (isCamera(device)) {
			const found = this.cameras.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isMount(device)) {
			const found = this.mounts.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isFocuser(device)) {
			const found = this.focusers.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isWheel(device)) {
			const found = this.wheels.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isRotator(device)) {
			const found = this.rotators.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		}
	}

	protected addConnection() {
		this.connectionDialog.edited = false
		this.connectionDialog.connection = structuredClone(DEFAULT_CONNECTION_DETAILS)
		this.connectionDialog.showDialog = true
	}

	protected editConnection(connection: ConnectionDetails, event: MouseEvent) {
		this.connectionDialog.edited = true
		this.connectionDialog.connection = connection
		this.connectionDialog.showDialog = true
		event.stopImmediatePropagation()
	}

	protected deleteConnection(connection: ConnectionDetails, event: MouseEvent) {
		const index = this.preference.connections.findIndex((e) => e === connection)

		if (index >= 0 && !connection.connected) {
			this.preference.connections.splice(index, 1)

			if (!this.preference.connections.length) {
				this.preference.connections.push(structuredClone(DEFAULT_CONNECTION_DETAILS))
			}

			if (connection === this.connection) {
				this.connection = this.preference.connections[0]
			}

			this.savePreference()
		}

		event.stopImmediatePropagation()
	}

	protected saveConnection() {
		if (!this.connectionDialog.edited) {
			this.connection = this.connectionDialog.connection
			this.preference.connections.push(this.connection)
		}

		this.savePreference()

		this.connectionDialog.showDialog = false
	}

	protected async connect() {
		try {
			if (this.connection && !this.connection.connected) {
				this.connection.id = await this.api.connect(this.connection.host, this.connection.port, this.connection.type)
			}
		} catch (e) {
			console.error(e)

			this.angularService.message('Connection failed', 'error')
		} finally {
			await this.updateConnection()
		}
	}

	protected async disconnect() {
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

	private async openDevice(type: DeviceType) {
		this.deviceModel.length = 0

		const devices: Device[] =
			type === 'CAMERA' ? this.cameras
			: type === 'MOUNT' ? this.mounts
			: type === 'FOCUSER' ? this.focusers
			: type === 'WHEEL' ? this.wheels
			: type === 'ROTATOR' ? this.rotators
			: []

		if (devices.length === 0) return

		const device = await this.deviceMenu.show(devices, undefined, type)

		if (device && device !== 'NONE') {
			await this.openDeviceWindow(device)
		}
	}

	private async openDeviceWindow(device: Device) {
		switch (device.type) {
			case 'MOUNT':
				await this.browserWindowService.openMount(device as Mount, { bringToFront: true })
				break
			case 'CAMERA':
				await this.browserWindowService.openCamera(device as Camera, { bringToFront: true })
				break
			case 'FOCUSER':
				await this.browserWindowService.openFocuser(device as Focuser, { bringToFront: true })
				break
			case 'WHEEL':
				await this.browserWindowService.openWheel(device as Wheel, { bringToFront: true })
				break
			case 'ROTATOR':
				await this.browserWindowService.openRotator(device as Rotator, { bringToFront: true })
				break
		}
	}

	private async openImage() {
		const path = await this.electronService.openImage({ defaultPath: this.preference.imagePath })

		if (path) {
			this.preference.imagePath = dirname(path)
			this.savePreference()

			await this.browserWindowService.openImage({ path, source: 'PATH' })
		}
	}

	protected async open(type: HomeWindowType) {
		switch (type) {
			case 'MOUNT':
			case 'CAMERA':
			case 'FOCUSER':
			case 'WHEEL':
			case 'ROTATOR':
				await this.openDevice(type)
				break
			case 'GUIDER':
				await this.browserWindowService.openGuider({ bringToFront: true })
				break
			case 'SKY_ATLAS':
				await this.browserWindowService.openSkyAtlas(undefined, { bringToFront: true })
				break
			case 'FRAMING':
				await this.browserWindowService.openFraming(undefined, { bringToFront: true })
				break
			case 'ALIGNMENT':
				await this.browserWindowService.openAlignment({ bringToFront: true })
				break
			case 'SEQUENCER':
				await this.browserWindowService.openSequencer({ bringToFront: true })
				break
			case 'AUTO_FOCUS':
				await this.browserWindowService.openAutoFocus({ bringToFront: true })
				break
			case 'FLAT_WIZARD':
				await this.browserWindowService.openFlatWizard({ bringToFront: true })
				break
			case 'STACKER':
				await this.browserWindowService.openStacker({ bringToFront: true })
				break
			case 'INDI':
				await this.browserWindowService.openINDI(undefined, { bringToFront: true })
				break
			case 'IMAGE':
				await this.openImage()
				break
			case 'SETTINGS':
				await this.browserWindowService.openSettings()
				break
			case 'CALCULATOR':
				await this.browserWindowService.openCalculator()
				break
			case 'CALIBRATION':
				await this.browserWindowService.openCalibration()
				break
			case 'ABOUT':
				await this.browserWindowService.openAbout()
				break
		}
	}

	private async updateConnection() {
		if (this.connection?.id) {
			try {
				const status = await this.api.connectionStatus(this.connection.id)

				if (status && !this.connection.connected) {
					this.connection.connectedAt = Date.now()
					this.savePreference()
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
				for (const connection of this.preference.connections) {
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

	protected scrolled(event: Event) {
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

		this.page = page

		event.stopImmediatePropagation()
	}

	protected scrollTo(event: Event, page: number) {
		this.page = page
		this.scrollToPage(page)
		event.stopImmediatePropagation()
	}

	protected scrollToPage(page: number) {
		const scrollChidren = document.getElementsByClassName('scroll-child')

		for (let i = 0; i < scrollChidren.length; i++) {
			const child = scrollChidren[i]

			if (scrollPageOf(child) === page) {
				child.scrollIntoView({ behavior: 'smooth' })
				break
			}
		}
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.home.get())

		this.preference.connections
			.sort((a, b) => (b.connectedAt ?? 0) - (a.connectedAt ?? 0))
			.forEach((e) => {
				e.id = undefined
				e.connected = false
			})

		if (!this.preference.connections.length) {
			this.preference.connections.push(structuredClone(DEFAULT_CONNECTION_DETAILS))
		}

		this.connection = this.preference.connections[0]
	}

	protected savePreference() {
		this.preferenceService.home.set(this.preference)
	}
}
