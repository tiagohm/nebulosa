import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { MenuItem, MenuItemCommandEvent, SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import {
	Camera,
	CameraDialogInput,
	CameraDialogMode,
	CameraDitherDialog,
	CameraLiveStackingDialog,
	CameraNamingFormatDialog,
	CameraPreference,
	CameraStartCapture,
	EMPTY_CAMERA,
	EMPTY_CAMERA_START_CAPTURE,
	ExposureMode,
	ExposureTimeUnit,
	FrameType,
	updateCameraStartCaptureFromCamera,
} from '../../shared/types/camera.types'
import { Device } from '../../shared/types/device.types'
import { Focuser } from '../../shared/types/focuser.types'
import { Equipment } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { resetCameraCaptureNamingFormat } from '../../shared/types/settings.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { Undefinable } from '../../shared/utils/types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-camera',
	templateUrl: './camera.component.html',
})
export class CameraComponent implements AfterContentInit, OnDestroy, Pingable {
	readonly camera = structuredClone(EMPTY_CAMERA)
	readonly equipment: Equipment = {}

	savePath = ''
	capturesPath = ''
	mode: CameraDialogMode = 'CAPTURE'

	get canShowMenu() {
		return this.mode === 'CAPTURE'
	}

	get canShowSavePath() {
		return this.mode === 'CAPTURE'
	}

	get canShowInfo() {
		return this.mode === 'CAPTURE'
	}

	get canExposureMode() {
		return this.mode === 'CAPTURE'
	}

	get canExposureTime() {
		return this.mode === 'CAPTURE' || this.mode === 'SEQUENCER' || this.mode === 'TPPA' || this.mode === 'AUTO_FOCUS'
	}

	get canExposureTimeUnit() {
		return this.mode !== 'DARV'
	}

	get canExposureAmount() {
		return this.mode === 'CAPTURE' || this.mode === 'SEQUENCER' || this.mode === 'AUTO_FOCUS'
	}

	get canFrameType() {
		return this.mode === 'CAPTURE' || this.mode === 'SEQUENCER'
	}

	get canStartOrAbort() {
		return this.mode === 'CAPTURE'
	}

	get canSave() {
		return this.mode !== 'CAPTURE'
	}

	calibrationModel: SlideMenuItem[] = []

	private readonly ditherMenuItem: SlideMenuItem = {
		icon: 'mdi mdi-pulse',
		label: 'Dither',
		slideMenu: [],
		command: () => {
			this.dither.showDialog = true
		},
	}

	private readonly liveStackingMenuItem: SlideMenuItem = {
		icon: 'mdi mdi-image-multiple',
		label: 'Live Stacking',
		slideMenu: [],
		command: () => {
			this.liveStacking.showDialog = true
		},
	}

	private readonly namingFormatMenuItem: SlideMenuItem = {
		icon: 'mdi mdi-rename',
		label: 'Naming Format',
		slideMenu: [],
		command: () => {
			this.namingFormat.showDialog = true
		},
	}

	private readonly snoopDevicesMenuItem: SlideMenuItem = {
		icon: 'mdi mdi-connection',
		label: 'Snoop Devices',
		slideMenu: [
			{
				icon: 'mdi mdi-telescope',
				label: 'Mount',
				slideMenu: [],
			},
			{
				icon: 'mdi mdi-palette',
				label: 'Filter Wheel',
				slideMenu: [],
			},
			{
				icon: 'mdi mdi-image-filter-center-focus',
				label: 'Focuser',
				slideMenu: [],
			},
			{
				icon: 'mdi mdi-rotate-right',
				label: 'Rotator',
				slideMenu: [],
			},
		],
	}

	readonly cameraModel: SlideMenuItem[] = [this.ditherMenuItem, this.liveStackingMenuItem, this.namingFormatMenuItem, this.snoopDevicesMenuItem]

	running = false
	hasDewHeater = false
	setpointTemperature = 0.0
	exposureTimeMin = 1
	exposureTimeMax = 1
	exposureTimeUnit = ExposureTimeUnit.MICROSECOND
	exposureMode: ExposureMode = 'SINGLE'
	subFrame = false

	readonly request = structuredClone(EMPTY_CAMERA_START_CAPTURE)

	readonly dither: CameraDitherDialog = {
		showDialog: false,
		request: this.request.dither,
	}

	readonly liveStacking: CameraLiveStackingDialog = {
		showDialog: false,
		request: this.request.liveStacking,
	}

	readonly namingFormat: CameraNamingFormatDialog = {
		showDialog: false,
		format: this.request.namingFormat,
	}

	readonly exposureTimeUnitModel: MenuItem[] = [
		{
			label: 'Minute (m)',
			command: () => {
				this.updateExposureUnit(ExposureTimeUnit.MINUTE)
				this.savePreference()
			},
		},
		{
			label: 'Second (s)',
			command: () => {
				this.updateExposureUnit(ExposureTimeUnit.SECOND)
				this.savePreference()
			},
		},
		{
			label: 'Millisecond (ms)',
			command: () => {
				this.updateExposureUnit(ExposureTimeUnit.MILLISECOND)
				this.savePreference()
			},
		},
		{
			label: 'Microsecond (µs)',
			command: () => {
				this.updateExposureUnit(ExposureTimeUnit.MICROSECOND)
				this.savePreference()
			},
		},
	]

	@ViewChild('cameraExposure')
	private readonly cameraExposure?: CameraExposureComponent

	get status() {
		return this.cameraExposure?.state ?? 'IDLE'
	}

	get pausingOrPaused() {
		return this.status === 'PAUSING' || this.status === 'PAUSED'
	}

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		private readonly browserWindow: BrowserWindowService,
		private readonly electron: ElectronService,
		private readonly preference: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Camera'

		electron.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera.id) {
				ngZone.run(() => {
					Object.assign(this.camera, event.device)
					this.update()
				})
			}
		})

		electron.on('CAMERA.DETACHED', (event) => {
			if (event.device.id === this.camera.id) {
				ngZone.run(() => {
					Object.assign(this.camera, EMPTY_CAMERA)
				})
			}
		})

		electron.on('CAMERA.CAPTURE_ELAPSED', (event) => {
			if (event.camera.id === this.camera.id) {
				ngZone.run(() => {
					this.running = this.cameraExposure?.handleCameraCaptureEvent(event) ?? false
				})
			}
		})

		electron.on('MOUNT.UPDATED', (event) => {
			if (event.device.id === this.equipment.mount?.id) {
				ngZone.run(() => {
					if (this.equipment.mount) {
						Object.assign(this.equipment.mount, event.device)
					}
				})
			}
		})

		electron.on('WHEEL.UPDATED', (event) => {
			if (event.device.id === this.equipment.wheel?.id) {
				ngZone.run(() => {
					if (this.equipment.wheel) {
						Object.assign(this.equipment.wheel, event.device)
					}
				})
			}
		})

		electron.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.equipment.focuser?.id) {
				ngZone.run(() => {
					if (this.equipment.focuser) {
						Object.assign(this.equipment.focuser, event.device)
					}
				})
			}
		})

		electron.on('ROTATOR.UPDATED', (event) => {
			if (event.device.id === this.equipment.rotator?.id) {
				ngZone.run(() => {
					if (this.equipment.rotator) {
						Object.assign(this.equipment.rotator, event.device)
					}
				})
			}
		})

		electron.on('CALIBRATION.CHANGED', async () => {
			await ngZone.run(() => this.loadCalibrationGroups())
		})

		electron.on('ROI.SELECTED', (event) => {
			if (event.camera.id === this.camera.id) {
				ngZone.run(() => {
					this.request.x = event.x
					this.request.y = event.y
					this.request.width = event.width
					this.request.height = event.height
				})
			}
		})

		this.snoopDevicesMenuItem.visible = !app.modal
	}

	ngAfterContentInit() {
		this.route.queryParams.subscribe(async (e) => {
			const decodedData = JSON.parse(decodeURIComponent(e['data'] as string)) as unknown

			if (this.app.modal) {
				await this.loadCameraStartCaptureForDialogMode(decodedData as CameraDialogInput)
			} else {
				await this.cameraChanged(decodedData as Camera)
			}

			this.pinger.register(this, 30000)

			if (!this.app.modal) {
				await this.loadEquipment()
			}

			await this.loadCalibrationGroups()
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)

		if (this.mode === 'CAPTURE') {
			void this.abortCapture()
		}
	}

	async ping() {
		if (this.camera.id) {
			await this.api.cameraListen(this.camera)
		}
	}

	private async loadCameraStartCaptureForDialogMode(data?: CameraDialogInput) {
		if (data) {
			this.mode = data.mode
			Object.assign(this.request, data.request)
			await this.cameraChanged(data.camera)
			this.loadDefaultsForMode(data.mode)
			this.normalizeExposureTimeAndUnit(this.request.exposureTime)
		}
	}

	private loadDefaultsForMode(mode: CameraDialogMode) {
		if (mode === 'SEQUENCER' || mode === 'AUTO_FOCUS') {
			this.exposureMode = 'FIXED'
		} else if (this.mode === 'FLAT_WIZARD') {
			this.exposureMode = 'SINGLE'
			this.request.frameType = 'FLAT'
		} else if (mode === 'TPPA') {
			this.exposureMode = 'FIXED'
			this.request.exposureAmount = 1
		} else if (mode === 'DARV') {
			this.exposureTimeUnit = ExposureTimeUnit.SECOND
		}
	}

	async cameraChanged(camera?: Camera) {
		if (camera?.id) {
			camera = await this.api.camera(camera.id)
			Object.assign(this.camera, camera)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = camera?.name ?? ''

		if (this.mode !== 'CAPTURE') {
			this.app.subTitle += ` · ${this.mode}`
		}
	}

	private async loadEquipment() {
		const makeItem = (selected: boolean, command: () => void, device?: Device) => {
			return {
				icon: device ? 'mdi mdi-connection' : 'mdi mdi-close',
				label: device?.name ?? 'None',
				selected,
				slideMenu: [],
				command: (event: MenuItemCommandEvent) => {
					command()
					this.preference.equipmentForDevice(this.camera).set(this.equipment)
					event.parentItem?.slideMenu?.forEach((item) => (item.selected = item === event.item))
				},
			} as SlideMenuItem
		}

		const slideMenu = this.snoopDevicesMenuItem.slideMenu

		// MOUNT

		const mounts = await this.api.mounts()
		this.equipment.mount = mounts.find((e) => e.name === this.equipment.mount?.name)

		const makeMountItem = (mount?: Mount) => {
			return makeItem(this.equipment.mount?.name === mount?.name, () => (this.equipment.mount = mount), mount)
		}

		slideMenu[0].slideMenu.push(makeMountItem())

		for (const mount of mounts) {
			slideMenu[0].slideMenu.push(makeMountItem(mount))
		}

		// FILTER WHEEL

		const wheels = await this.api.wheels()
		this.equipment.wheel = wheels.find((e) => e.name === this.equipment.wheel?.name)

		const makeWheelItem = (wheel?: FilterWheel) => {
			return makeItem(this.equipment.wheel?.name === wheel?.name, () => (this.equipment.wheel = wheel), wheel)
		}

		slideMenu[1].slideMenu.push(makeWheelItem())

		for (const wheel of wheels) {
			slideMenu[1].slideMenu.push(makeWheelItem(wheel))
		}

		// FOCUSER

		const focusers = await this.api.focusers()
		this.equipment.focuser = focusers.find((e) => e.name === this.equipment.focuser?.name)

		const makeFocuserItem = (focuser?: Focuser) => {
			return makeItem(this.equipment.focuser?.name === focuser?.name, () => (this.equipment.focuser = focuser), focuser)
		}

		slideMenu[2].slideMenu.push(makeFocuserItem())

		for (const focuser of focusers) {
			slideMenu[2].slideMenu.push(makeFocuserItem(focuser))
		}

		// ROTATOR

		const rotators = await this.api.rotators()
		this.equipment.rotator = rotators.find((e) => e.name === this.equipment.rotator?.name)

		const makeRotatorItem = (rotator?: Rotator) => {
			return makeItem(this.equipment.rotator?.name === rotator?.name, () => (this.equipment.rotator = rotator), rotator)
		}

		slideMenu[3].slideMenu.push(makeRotatorItem())

		for (const rotator of rotators) {
			slideMenu[3].slideMenu.push(makeRotatorItem(rotator))
		}
	}

	private async loadCalibrationGroups() {
		const groups = await this.api.calibrationGroups()
		const found = !!groups.find((e) => this.request.calibrationGroup === e)

		if (!found) {
			this.request.calibrationGroup = undefined
		}

		const makeItem = (name?: string) => {
			return {
				label: name ?? 'None',
				icon: name ? 'mdi mdi-wrench' : 'mdi mdi-close',
				selected: this.request.calibrationGroup === name,
				slideMenu: [],
				command: () => {
					this.request.calibrationGroup = name
					this.savePreference()
					void this.loadCalibrationGroups()
				},
			} as SlideMenuItem
		}

		const menu: SlideMenuItem[] = []

		menu.push({
			icon: 'mdi mdi-wrench',
			label: 'Open Calibration',
			slideMenu: [],
			command: () => {
				return this.browserWindow.openCalibration({ bringToFront: true })
			},
		})

		menu.push(SEPARATOR_MENU_ITEM)
		menu.push(makeItem())

		for (const group of groups) {
			menu.push(makeItem(group))
		}

		this.calibrationModel = menu
	}

	connect() {
		if (this.camera.connected) {
			return this.api.cameraDisconnect(this.camera)
		} else {
			return this.api.cameraConnect(this.camera)
		}
	}

	toggleAutoSaveAllExposures() {
		this.request.autoSave = !this.request.autoSave
		this.savePreference()
	}

	toggleAutoSubFolder() {
		switch (this.request.autoSubFolderMode) {
			case 'OFF':
				this.request.autoSubFolderMode = 'NOON'
				break
			case 'NOON':
				this.request.autoSubFolderMode = 'MIDNIGHT'
				break
			case 'MIDNIGHT':
				this.request.autoSubFolderMode = 'OFF'
				break
		}

		this.savePreference()
	}

	async chooseSavePath() {
		const defaultPath = this.savePath || this.capturesPath
		const path = await this.electron.openDirectory({ defaultPath })

		if (path) {
			this.savePath = path
			this.savePreference()
		}
	}

	applySetpointTemperature() {
		this.savePreference()
		return this.api.cameraSetpointTemperature(this.camera, this.setpointTemperature)
	}

	toggleCooler() {
		return this.api.cameraCooler(this.camera, this.camera.cooler)
	}

	fullsize() {
		this.request.x = this.camera.minX
		this.request.y = this.camera.minY
		this.request.width = this.camera.maxWidth
		this.request.height = this.camera.maxHeight
		this.savePreference()
	}

	openMount(mount: Mount) {
		return this.browserWindow.openMount(mount)
	}

	openFocuser(focuser: Focuser) {
		return this.browserWindow.openFocuser(focuser)
	}

	openWheel(wheel: FilterWheel) {
		return this.browserWindow.openWheel(wheel)
	}

	openRotator(rotator: Rotator) {
		return this.browserWindow.openRotator(rotator)
	}

	openCameraImage() {
		return this.browserWindow.openCameraImage(this.camera, 'CAMERA', this.request)
	}

	private makeCameraStartCapture(): CameraStartCapture {
		const x = this.subFrame ? this.request.x : this.camera.minX
		const y = this.subFrame ? this.request.y : this.camera.minY
		const width = this.subFrame ? this.request.width : this.camera.maxWidth
		const height = this.subFrame ? this.request.height : this.camera.maxHeight
		const exposureFactor = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
		const exposureTime = Math.trunc((this.request.exposureTime * 60000000) / exposureFactor)
		const exposureAmount =
			this.exposureMode === 'LOOP' ? 0
			: this.exposureMode === 'FIXED' ? this.request.exposureAmount
			: 1
		const savePath = this.mode !== 'CAPTURE' ? this.request.savePath : this.savePath

		const liveStackingRequest = this.preference.liveStackingRequest(this.request.liveStacking.type).get()
		this.request.liveStacking.executablePath = liveStackingRequest.executablePath
		this.request.liveStacking.slot = liveStackingRequest.slot || 1

		let shutterPosition: Undefinable<number>

		if (this.equipment.wheel) {
			const wheelPreference = this.preference.wheelPreference(this.equipment.wheel).get()
			shutterPosition = wheelPreference.shutterPosition
		}

		return {
			...this.request,
			shutterPosition,
			x,
			y,
			width,
			height,
			exposureTime,
			exposureAmount,
			savePath,
		}
	}

	async startCapture() {
		try {
			this.running = true
			await this.openCameraImage()
			await this.api.cameraStartCapture(this.camera, this.makeCameraStartCapture(), this.equipment)
			this.preference.equipmentForDevice(this.camera).set(this.equipment)
		} catch {
			this.running = false
		}
	}

	pauseCapture() {
		return this.api.cameraPauseCapture(this.camera)
	}

	unpauseCapture() {
		return this.api.cameraUnpauseCapture(this.camera)
	}

	abortCapture() {
		return this.api.cameraAbortCapture(this.camera)
	}

	static exposureUnitFactor(unit: ExposureTimeUnit) {
		switch (unit) {
			case ExposureTimeUnit.MINUTE:
				return 1
			case ExposureTimeUnit.SECOND:
				return 60
			case ExposureTimeUnit.MILLISECOND:
				return 60000
			case ExposureTimeUnit.MICROSECOND:
				return 60000000
			default:
				return 0
		}
	}

	private updateExposureUnit(unit: ExposureTimeUnit, from: ExposureTimeUnit = this.exposureTimeUnit) {
		const exposureMax = this.camera.exposureMax || 60000000

		if (exposureMax) {
			const a = CameraComponent.exposureUnitFactor(from)
			const b = CameraComponent.exposureUnitFactor(unit)
			const exposureTime = Math.trunc((this.request.exposureTime * b) / a)
			const exposureTimeMin = Math.trunc((this.camera.exposureMin * b) / 60000000)
			const exposureTimeMax = Math.trunc((exposureMax * b) / 60000000)
			this.exposureTimeMax = Math.max(1, exposureTimeMax)
			this.exposureTimeMin = Math.max(1, exposureTimeMin)
			this.request.exposureTime = Math.max(this.exposureTimeMin, Math.min(exposureTime, this.exposureTimeMax))
			this.exposureTimeUnit = unit
		}
	}

	private normalizeExposureTimeAndUnit(exposureTime: number) {
		if (this.canExposureTimeUnit) {
			const factors = [
				{ unit: ExposureTimeUnit.MINUTE, time: 60000000 },
				{ unit: ExposureTimeUnit.SECOND, time: 1000000 },
				{ unit: ExposureTimeUnit.MILLISECOND, time: 1000 },
			]

			for (const { unit, time } of factors) {
				if (exposureTime >= time) {
					const k = exposureTime / time

					// exposureTime is multiple of time.
					if (k === Math.floor(k)) {
						this.updateExposureUnit(unit, ExposureTimeUnit.MICROSECOND)
						return
					}
				}
			}
		} else {
			this.updateExposureUnit(this.exposureTimeUnit, ExposureTimeUnit.MICROSECOND)
		}
	}

	private update() {
		if (this.camera.id) {
			if (this.camera.connected) {
				updateCameraStartCaptureFromCamera(this.request, this.camera)
				this.updateExposureUnit(this.exposureTimeUnit)
			}

			this.capturesPath = this.camera.capturesPath
		}
	}

	clearSavePath() {
		this.savePath = ''
		this.savePreference()
	}

	resetCameraCaptureNamingFormat(type: FrameType) {
		const namingFormatPreference = this.preference.cameraCaptureNamingFormatPreference.get()
		resetCameraCaptureNamingFormat(type, this.namingFormat.format, namingFormatPreference)
		this.savePreference()
	}

	apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.camera.name) {
			const cameraPreference: Partial<CameraPreference> = this.preference.cameraPreference(this.camera).get()

			this.request.autoSave = cameraPreference.autoSave ?? false
			this.savePath = cameraPreference.savePath ?? ''
			this.request.autoSubFolderMode = cameraPreference.autoSubFolderMode ?? 'OFF'
			this.setpointTemperature = cameraPreference.setpointTemperature ?? 0
			this.request.exposureTime = cameraPreference.exposureTime ?? this.camera.exposureMin
			this.exposureTimeUnit = cameraPreference.exposureTimeUnit ?? ExposureTimeUnit.MICROSECOND
			this.exposureMode = cameraPreference.exposureMode ?? 'SINGLE'
			this.request.exposureDelay = cameraPreference.exposureDelay ?? 0
			this.request.exposureAmount = cameraPreference.exposureAmount ?? 1
			this.request.x = cameraPreference.x ?? this.camera.minX
			this.request.y = cameraPreference.y ?? this.camera.minY
			this.request.width = cameraPreference.width ?? this.camera.maxWidth
			this.request.height = cameraPreference.height ?? this.camera.maxHeight
			this.subFrame = cameraPreference.subFrame ?? false
			this.request.binX = cameraPreference.binX ?? 1
			this.request.binY = cameraPreference.binY ?? 1
			this.request.frameType = cameraPreference.frameType ?? 'LIGHT'
			this.request.gain = cameraPreference.gain ?? 0
			this.request.offset = cameraPreference.offset ?? 0
			this.request.frameFormat = cameraPreference.frameFormat ?? (this.camera.frameFormats[0] || '')
			this.request.calibrationGroup = cameraPreference.calibrationGroup

			this.request.dither.enabled = cameraPreference.dither?.enabled ?? false
			this.request.dither.amount = cameraPreference.dither?.amount ?? 1.5
			this.request.dither.raOnly = cameraPreference.dither?.raOnly ?? false
			this.request.dither.afterExposures = cameraPreference.dither?.afterExposures ?? 1

			this.request.liveStacking.enabled = cameraPreference.liveStacking?.enabled ?? false
			this.request.liveStacking.type = cameraPreference.liveStacking?.type ?? 'SIRIL'
			this.request.liveStacking.executablePath = cameraPreference.liveStacking?.executablePath ?? ''
			this.request.liveStacking.darkPath = cameraPreference.liveStacking?.darkPath
			this.request.liveStacking.flatPath = cameraPreference.liveStacking?.flatPath
			this.request.liveStacking.biasPath = cameraPreference.liveStacking?.biasPath
			this.request.liveStacking.use32Bits = cameraPreference.liveStacking?.use32Bits ?? false
			this.request.liveStacking.slot = cameraPreference.liveStacking?.slot ?? 1

			const cameraCaptureNamingFormatPreference = this.preference.cameraCaptureNamingFormatPreference.get()
			this.request.namingFormat.light = cameraPreference.namingFormat?.light ?? cameraCaptureNamingFormatPreference.light
			this.request.namingFormat.dark = cameraPreference.namingFormat?.dark ?? cameraCaptureNamingFormatPreference.dark
			this.request.namingFormat.flat = cameraPreference.namingFormat?.flat ?? cameraCaptureNamingFormatPreference.flat
			this.request.namingFormat.bias = cameraPreference.namingFormat?.bias ?? cameraCaptureNamingFormatPreference.bias

			Object.assign(this.equipment, this.preference.equipmentForDevice(this.camera).get())
		}
	}

	savePreference() {
		if (this.mode === 'CAPTURE' && this.camera.connected) {
			const preference: CameraPreference = {
				...this.request,
				setpointTemperature: this.setpointTemperature,
				exposureTimeUnit: this.exposureTimeUnit,
				exposureMode: this.exposureMode,
				subFrame: this.subFrame,
				savePath: this.request.savePath || this.savePath,
			}

			this.preference.cameraPreference(this.camera).set(preference)
		}
	}

	static async showAsDialog(window: BrowserWindowService, mode: CameraDialogMode, camera: Camera, request: CameraStartCapture) {
		const result = await window.openCameraDialog({ mode, camera, request })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
