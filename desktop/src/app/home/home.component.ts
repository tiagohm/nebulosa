import { AfterContentInit, Component, inject, NgZone, ViewChild, ViewEncapsulation } from '@angular/core'
import nebulosa from '../../assets/data/nebulosa.json'
import { DeviceChooserComponent } from '../../shared/components/device-chooser/device-chooser.component'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { MenuItem, SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, isCamera } from '../../shared/types/camera.types'
import { Device, DeviceType } from '../../shared/types/device.types'
import { DustCap, isDustCap } from '../../shared/types/dustcap.types'
import { Focuser, isFocuser } from '../../shared/types/focuser.types'
import { GuideOutput, isGuideOuptut } from '../../shared/types/guider.types'
import { ConnectionDetails, DEFAULT_CONNECTION_DETAILS, DEFAULT_HOME_CONNECTION_DIALOG, DEFAULT_HOME_PREFERENCE, HomeWindowType } from '../../shared/types/home.types'
import { isLightBox, LightBox } from '../../shared/types/lightbox.types'
import { isMount, Mount } from '../../shared/types/mount.types'
import { isRotator, Rotator } from '../../shared/types/rotator.types'
import { isWheel, Wheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

function scrollPageOf(element: Element) {
	return parseInt(element.getAttribute('scroll-page') ?? '0')
}

@Component({
	selector: 'neb-home',
	templateUrl: 'home.component.html',
	styleUrls: ['home.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class HomeComponent implements AfterContentInit {
	private readonly electronService = inject(ElectronService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly api = inject(ApiService)
	private readonly preferenceService = inject(PreferenceService)

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
	protected guideOutputs: GuideOutput[] = []
	protected lightBoxes: LightBox[] = []
	protected dustCaps: DustCap[] = []

	protected page = 0
	protected newVersion?: string

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

	get hasRotator() {
		return this.rotators.length > 0
	}

	get hasGuideOutput() {
		return this.guideOutputs.length > 0
	}

	get hasSwitch() {
		return this.switches.length > 0
	}

	get hasLightBox() {
		return this.lightBoxes.length > 0
	}

	get hasDustCap() {
		return this.dustCaps.length > 0
	}

	get hasAuxiliary() {
		return this.hasSwitch || this.hasLightBox || this.hasDustCap
	}

	get hasGuider() {
		return (this.hasCamera && this.hasMount) || this.hasGuideOutput
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
		return this.hasCamera || this.hasMount || this.hasFocuser || this.hasWheel || this.hasDome || this.hasRotator || this.hasSwitch || this.hasGuideOutput || this.hasLightBox || this.hasDustCap
	}

	get hasINDI() {
		return this.connection?.type === 'INDI' && this.hasDevices
	}

	get hasAlpaca() {
		return this.connection?.type === 'ALPACA' && this.hasDevices
	}

	constructor() {
		const app = inject(AppComponent)
		const ngZone = inject(NgZone)

		app.title = 'Nebulosa'

		this.electronService.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`CAMERA.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`CAMERA.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('MOUNT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`MOUNT.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`MOUNT.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('FOCUSER.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`FOCUSER.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`FOCUSER.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('WHEEL.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`WHEEL.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`WHEEL.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('ROTATOR.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`ROTATOR.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`ROTATOR.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('GUIDE_OUTPUT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`GUIDE_OUTPUT.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`GUIDE_OUTPUT.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('LIGHT_BOX.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`LIGHT_BOX.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`LIGHT_BOX.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('DUST_CAP.ATTACHED', (event) => {
			ngZone.run(() => {
				this.deviceAdded(event.device)
			})
		})
		this.electronService.on(`DUST_CAP.DETACHED`, (event) => {
			ngZone.run(() => {
				this.deviceRemoved(event.device)
			})
		})
		this.electronService.on(`DUST_CAP.UPDATED`, (event) => {
			ngZone.run(() => {
				this.deviceUpdated(event.device)
			})
		})

		this.electronService.on('CONNECTION.CLOSED', async (event) => {
			if (this.connection?.id === event.id) {
				await ngZone.run(() => {
					return this.updateConnection()
				})
			}
		})

		this.electronService.on('IMAGE.OPEN', (event) => {
			return ngZone.run(() => {
				return this.browserWindowService.openImage({ path: event.path, source: 'PATH' })
			})
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
			this.guideOutputs = await this.api.guideOutputs()
			this.lightBoxes = await this.api.lightBoxes()
			this.dustCaps = await this.api.dustCaps()
		}

		void this.checkForNewVersion()
	}

	private async checkForNewVersion() {
		if (this.preferenceService.settings.get().checkVersion) {
			try {
				const release = await this.api.latestRelease()

				if (release.tag_name && nebulosa.version !== release.tag_name) {
					this.newVersion = release.name
				}
			} catch {
				console.error('failed to check for new version')
			}
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
		} else if (isGuideOuptut(device)) {
			this.guideOutputs.push(device)
		} else if (isLightBox(device)) {
			this.lightBoxes.push(device)
		} else if (isDustCap(device)) {
			this.dustCaps.push(device)
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
		} else if (isGuideOuptut(device)) {
			const found = this.guideOutputs.findIndex((e) => e.id === device.id)
			this.guideOutputs.splice(found, 1)
		} else if (isLightBox(device)) {
			const found = this.lightBoxes.findIndex((e) => e.id === device.id)
			this.lightBoxes.splice(found, 1)
		} else if (isDustCap(device)) {
			const found = this.dustCaps.findIndex((e) => e.id === device.id)
			this.dustCaps.splice(found, 1)
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
		} else if (isGuideOuptut(device)) {
			const found = this.guideOutputs.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isLightBox(device)) {
			const found = this.lightBoxes.find((e) => e.id === device.id)
			found && Object.assign(found, device)
		} else if (isDustCap(device)) {
			const found = this.dustCaps.find((e) => e.id === device.id)
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
		} finally {
			await this.updateConnection()
		}
	}

	protected async disconnect() {
		try {
			if (this.connection?.id && this.connection.connected) {
				await this.api.disconnect(this.connection.id)
			}
		} finally {
			await this.updateConnection()
		}
	}

	protected deviceConnected(event: DeviceConnectionCommandEvent) {
		return DeviceChooserComponent.handleConnectDevice(this.api, event.device, event.item)
	}

	protected deviceDisconnected(event: DeviceConnectionCommandEvent) {
		return DeviceChooserComponent.handleDisconnectDevice(this.api, event.device, event.item)
	}

	protected toggleAuxiliary() {
		this.preference.showAuxiliary = !this.preference.showAuxiliary
		this.savePreference()
	}

	private async openDevice(type: DeviceType) {
		this.deviceModel.length = 0

		const devices: Device[] =
			type === 'CAMERA' ? this.cameras
			: type === 'MOUNT' ? this.mounts
			: type === 'FOCUSER' ? this.focusers
			: type === 'WHEEL' ? this.wheels
			: type === 'ROTATOR' ? this.rotators
			: type === 'LIGHT_BOX' ? this.lightBoxes
			: type === 'DUST_CAP' ? this.dustCaps
			: []

		if (devices.length === 0) return

		const device = await this.deviceMenu.show(devices, undefined, type)

		if (device && device !== 'NONE') {
			await this.openDeviceWindow(device)
		}
	}

	private async openDeviceWindow(device: Device) {
		if (isMount(device)) {
			await this.browserWindowService.openMount(device, { bringToFront: true })
		} else if (isCamera(device)) {
			await this.browserWindowService.openCamera(device, { bringToFront: true })
		} else if (isFocuser(device)) {
			await this.browserWindowService.openFocuser(device, { bringToFront: true })
		} else if (isWheel(device)) {
			await this.browserWindowService.openWheel(device, { bringToFront: true })
		} else if (isRotator(device)) {
			await this.browserWindowService.openRotator(device, { bringToFront: true })
		} else if (isLightBox(device)) {
			await this.browserWindowService.openLightBox(device, { bringToFront: true })
		} else if (isDustCap(device)) {
			await this.browserWindowService.openDustCap(device, { bringToFront: true })
		}
	}

	private async openImage() {
		const path = await this.electronService.openImage({ defaultPath: this.preference.imagePath })

		if (path) {
			this.preference.imagePath = window.path.dirname(path)
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
			case 'LIGHT_BOX':
			case 'DUST_CAP':
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
			case 'SEQUENCER': {
				const device = await this.deviceMenu.show(this.cameras, undefined, 'CAMERA')

				if (device && device !== 'NONE') {
					await this.browserWindowService.openSequencer(device, { bringToFront: true })
				}

				break
			}
			case 'AUTO_FOCUS':
				await this.browserWindowService.openAutoFocus({ bringToFront: true })
				break
			case 'FLAT_WIZARD':
				await this.browserWindowService.openFlatWizard({ bringToFront: true })
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
						connection.connectedAt = Date.now()
						this.savePreference()
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
			this.lightBoxes = []
			this.dustCaps = []
			this.guideOutputs = []
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
