import type { OnDestroy } from '@angular/core'
import { Component, effect, HostListener, inject, NgZone, viewChild } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import type { CameraExposureComponent } from '../../shared/components/camera-exposure.component'
import type { MenuItemCommandEvent, SlideMenuItem } from '../../shared/components/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import type { Tickable } from '../../shared/services/ticker.service'
import { Ticker } from '../../shared/services/ticker.service'
import type { Camera, CameraDialogInput, CameraDitherDialog, CameraLiveStackingDialog, CameraMode, CameraNamingFormatDialog, CameraStartCapture, FrameType } from '../../shared/types/camera.types'
import { cameraCaptureNamingFormatWithDefault, DEFAULT_CAMERA, DEFAULT_CAMERA_PREFERENCE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import type { Device, DeviceType } from '../../shared/types/device.types'
import type { Focuser } from '../../shared/types/focuser.types'
import type { Mount } from '../../shared/types/mount.types'
import type { Rotator } from '../../shared/types/rotator.types'
import { resetCameraCaptureNamingFormat } from '../../shared/types/settings.types'
import type { Wheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-camera',
	templateUrl: 'camera.component.html',
})
export class CameraComponent implements OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly electronService = inject(ElectronService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)
	private readonly data = injectQueryParams('data', { transform: (v) => v && decodeURIComponent(v) })

	protected readonly camera = structuredClone(DEFAULT_CAMERA)
	protected calibrationModel: SlideMenuItem[] = []

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

	protected readonly cameraModel: SlideMenuItem[] = [this.ditherMenuItem, this.liveStackingMenuItem, this.namingFormatMenuItem, this.snoopDevicesMenuItem]

	protected running = false
	protected hasDewHeater = false
	protected readonly preference = structuredClone(DEFAULT_CAMERA_PREFERENCE)
	protected request = this.preference.request
	protected mode: CameraMode = 'CAPTURE'

	protected readonly dither: CameraDitherDialog = {
		showDialog: false,
		request: this.request.dither,
	}

	protected readonly liveStacking: CameraLiveStackingDialog = {
		showDialog: false,
		request: this.request.liveStacking,
	}

	protected readonly namingFormat: CameraNamingFormatDialog = {
		showDialog: false,
		format: this.request.namingFormat,
	}

	private readonly cameraExposure = viewChild<CameraExposureComponent>('cameraExposure')

	get status() {
		return this.cameraExposure()?.currentState ?? 'IDLE'
	}

	get pausingOrPaused() {
		return this.status === 'PAUSING' || this.status === 'PAUSED'
	}

	get hasCalibration() {
		return !this.app.modal
	}

	get hasLiveStacking() {
		return !this.app.modal
	}

	get hasDither() {
		return !this.app.modal
	}

	get canSnoopDevices() {
		return !this.app.modal
	}

	get canShowMenu() {
		return this.hasCalibration || this.hasLiveStacking || this.hasDither || this.canSnoopDevices
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

	get currentWheelFilter() {
		return this.preference.wheel?.names[this.preference.wheel.position - 1]
	}

	constructor() {
		const ngZone = inject(NgZone)

		this.app.title = 'Camera'

		this.electronService.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera.id) {
				ngZone.run(() => {
					Object.assign(this.camera, event.device)
					this.update()
				})
			}
		})

		this.electronService.on('CAMERA.DETACHED', (event) => {
			if (event.device.id === this.camera.id) {
				ngZone.run(() => {
					Object.assign(this.camera, DEFAULT_CAMERA)
				})
			}
		})

		this.electronService.on('CAMERA.CAPTURE_ELAPSED', (event) => {
			if (event.camera.id === this.camera.id) {
				ngZone.run(() => {
					this.running = this.cameraExposure()?.handleCameraCaptureEvent(event) ?? false
				})
			}
		})

		this.electronService.on('MOUNT.UPDATED', (event) => {
			if (this.mode === 'CAPTURE' && event.device.id === this.preference.mount?.id) {
				ngZone.run(() => {
					if (this.preference.mount) {
						Object.assign(this.preference.mount, event.device)
					}
				})
			}
		})

		this.electronService.on('MOUNT.ATTACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('MOUNT')
				})
			}
		})

		this.electronService.on('MOUNT.DETACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('MOUNT')
				})
			}
		})

		this.electronService.on('WHEEL.UPDATED', (event) => {
			if (this.mode === 'CAPTURE' && event.device.id === this.preference.wheel?.id) {
				ngZone.run(() => {
					if (this.preference.wheel) {
						Object.assign(this.preference.wheel, event.device)
					}
				})
			}
		})

		this.electronService.on('WHEEL.ATTACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('WHEEL')
				})
			}
		})

		this.electronService.on('WHEEL.DETACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('WHEEL')
				})
			}
		})

		this.electronService.on('FOCUSER.UPDATED', (event) => {
			if (this.mode === 'CAPTURE' && event.device.id === this.preference.focuser?.id) {
				ngZone.run(() => {
					if (this.preference.focuser) {
						Object.assign(this.preference.focuser, event.device)
					}
				})
			}
		})

		this.electronService.on('FOCUSER.ATTACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('FOCUSER')
				})
			}
		})

		this.electronService.on('FOCUSER.DETACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('FOCUSER')
				})
			}
		})

		this.electronService.on('ROTATOR.UPDATED', (event) => {
			if (this.mode === 'CAPTURE' && event.device.id === this.preference.rotator?.id) {
				ngZone.run(() => {
					if (this.preference.rotator) {
						Object.assign(this.preference.rotator, event.device)
					}
				})
			}
		})

		this.electronService.on('ROTATOR.ATTACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('ROTATOR')
				})
			}
		})

		this.electronService.on('ROTATOR.DETACHED', () => {
			if (this.mode === 'CAPTURE') {
				void ngZone.run(() => {
					return this.loadEquipment('ROTATOR')
				})
			}
		})

		this.electronService.on('CALIBRATION.CHANGED', () => {
			void ngZone.run(() => this.loadCalibrationGroups())
		})

		this.electronService.on('ROI.SELECTED', (event) => {
			if (event.camera.id === this.camera.id) {
				ngZone.run(() => {
					this.request.x = event.x
					this.request.y = event.y
					this.request.width = event.width
					this.request.height = event.height
					this.preference.subFrame = true
				})
			}
		})

		this.snoopDevicesMenuItem.visible = this.canSnoopDevices

		effect(async () => {
			const data = this.data()

			if (data) {
				if (this.app.modal) {
					await this.loadCameraStartCaptureForDialogMode(JSON.parse(data))
				} else {
					await this.cameraChanged(JSON.parse(data))
				}

				this.ticker.register(this, 30000)

				if (this.mode === 'CAPTURE') {
					await this.loadEquipment()
					await this.loadCalibrationGroups()
				}
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)

		if (this.mode === 'CAPTURE') {
			void this.abortCapture()
		}
	}

	async tick() {
		if (this.camera.id) await this.api.cameraListen(this.camera)
		if (this.preference.wheel?.id) await this.api.wheelListen(this.preference.wheel)
		if (this.preference.focuser?.id) await this.api.focuserListen(this.preference.focuser)
		if (this.preference.mount?.id) await this.api.mountListen(this.preference.mount)
		if (this.preference.rotator?.id) await this.api.rotatorListen(this.preference.rotator)
	}

	private async loadCameraStartCaptureForDialogMode(data?: CameraDialogInput) {
		if (data) {
			this.mode = data.mode
			await this.cameraChanged(data.camera)
			Object.assign(this.request, data.request)
			this.loadDefaultsForMode(this.mode)
		}
	}

	private loadDefaultsForMode(mode: CameraMode) {
		if (mode === 'SEQUENCER' || mode === 'AUTO_FOCUS') {
			this.preference.exposureMode = 'FIXED'
		} else if (this.mode === 'FLAT_WIZARD') {
			this.preference.exposureMode = 'SINGLE'
			this.request.frameType = 'FLAT'
		} else if (mode === 'TPPA') {
			this.preference.exposureMode = 'FIXED'
			this.request.exposureAmount = 1
		} else if (mode === 'DARV') {
			this.preference.exposureTimeUnit = 'SECOND'
		}

		this.ditherMenuItem.visible = this.hasDither
		this.liveStackingMenuItem.visible = this.hasLiveStacking
	}

	private updateSubTitle() {
		let subTitle = this.camera.name

		if (this.mode !== 'CAPTURE') {
			subTitle += ` · ${this.mode}`
		}

		this.app.subTitle = subTitle
	}

	protected async cameraChanged(camera?: Camera) {
		if (camera?.id) {
			camera = await this.api.camera(camera.id)
			Object.assign(this.camera, camera)

			this.loadPreference()
			this.update()
		}

		this.updateSubTitle()
	}

	private async loadEquipment(type?: DeviceType) {
		const makeMenuItem = (selected: boolean, command: () => Promise<void> | void, device?: Device) => {
			return {
				icon: device ? 'mdi mdi-connection' : 'mdi mdi-close',
				label: device?.name ?? 'None',
				selected,
				slideMenu: [],
				command: async (event: MenuItemCommandEvent) => {
					await command()
					this.savePreference()
					event.parentItem?.slideMenu?.forEach((item) => (item.selected = item === event.item))
				},
			} as SlideMenuItem
		}

		const menu = this.snoopDevicesMenuItem.slideMenu

		// MOUNT

		if (!type || type === 'MOUNT') {
			menu[0].slideMenu.length = 0

			const mounts = await this.api.mounts()
			this.preference.mount = mounts.find((e) => e.name === this.preference.mount?.name)

			const makeMountItem = (mount?: Mount) => {
				return makeMenuItem(
					this.preference.mount?.name === mount?.name,
					async () => {
						this.preference.mount = mount && (await this.api.mount(mount.id))
					},
					mount,
				)
			}

			menu[0].slideMenu.push(makeMountItem())

			for (const mount of mounts) {
				menu[0].slideMenu.push(makeMountItem(mount))
			}
		}

		// WHEEL

		if (!type || type === 'WHEEL') {
			menu[1].slideMenu.length = 0

			const wheels = await this.api.wheels()
			this.preference.wheel = wheels.find((e) => e.name === this.preference.wheel?.name)

			const makeWheelItem = (wheel?: Wheel) => {
				return makeMenuItem(
					this.preference.wheel?.name === wheel?.name,
					async () => {
						this.preference.wheel = wheel && (await this.api.wheel(wheel.id))
					},
					wheel,
				)
			}

			menu[1].slideMenu.push(makeWheelItem())

			for (const wheel of wheels) {
				menu[1].slideMenu.push(makeWheelItem(wheel))
			}
		}

		// FOCUSER

		if (!type || type === 'FOCUSER') {
			menu[2].slideMenu.length = 0

			const focusers = await this.api.focusers()
			this.preference.focuser = focusers.find((e) => e.name === this.preference.focuser?.name)

			const makeFocuserItem = (focuser?: Focuser) => {
				return makeMenuItem(
					this.preference.focuser?.name === focuser?.name,
					async () => {
						this.preference.focuser = focuser && (await this.api.focuser(focuser.id))
					},
					focuser,
				)
			}

			menu[2].slideMenu.push(makeFocuserItem())

			for (const focuser of focusers) {
				menu[2].slideMenu.push(makeFocuserItem(focuser))
			}
		}

		// ROTATOR

		if (!type || type === 'ROTATOR') {
			menu[3].slideMenu.length = 0

			const rotators = await this.api.rotators()
			this.preference.rotator = rotators.find((e) => e.name === this.preference.rotator?.name)

			const makeRotatorItem = (rotator?: Rotator) => {
				return makeMenuItem(
					this.preference.rotator?.name === rotator?.name,
					async () => {
						this.preference.rotator = rotator && (await this.api.rotator(rotator.id))
					},
					rotator,
				)
			}

			menu[3].slideMenu.push(makeRotatorItem())

			for (const rotator of rotators) {
				menu[3].slideMenu.push(makeRotatorItem(rotator))
			}
		}

		await this.tick()
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
				return this.browserWindowService.openCalibration({ bringToFront: true })
			},
		})

		menu.push(SEPARATOR_MENU_ITEM)
		menu.push(makeItem())

		for (const group of groups) {
			menu.push(makeItem(group))
		}

		this.calibrationModel = menu
	}

	protected connect() {
		if (this.camera.connected) {
			return this.api.cameraDisconnect(this.camera)
		} else {
			return this.api.cameraConnect(this.camera)
		}
	}

	protected toggleAutoSaveAllExposures() {
		this.request.autoSave = !this.request.autoSave
		this.savePreference()
	}

	protected toggleAutoSubFolder() {
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

	protected async chooseSavePath() {
		const defaultPath = this.preference.request.savePath || this.camera.capturesPath
		const path = await this.electronService.openDirectory({ defaultPath })

		if (path) {
			this.preference.request.savePath = path
			this.savePreference()
		}
	}

	protected applySetpointTemperature() {
		this.savePreference()
		return this.api.cameraSetpointTemperature(this.camera, this.preference.setpointTemperature)
	}

	protected toggleCooler() {
		return this.api.cameraCooler(this.camera, this.camera.cooler)
	}

	protected fullsize() {
		this.request.x = this.camera.minX
		this.request.y = this.camera.minY
		this.request.width = this.camera.maxWidth
		this.request.height = this.camera.maxHeight
		this.savePreference()
	}

	protected openMount(mount: Mount) {
		return this.browserWindowService.openMount(mount, { bringToFront: true })
	}

	protected openFocuser(focuser: Focuser) {
		return this.browserWindowService.openFocuser(focuser, { bringToFront: true })
	}

	protected openWheel(wheel: Wheel) {
		return this.browserWindowService.openWheel(wheel, { bringToFront: true })
	}

	protected openRotator(rotator: Rotator) {
		return this.browserWindowService.openRotator(rotator, { bringToFront: true })
	}

	protected openCameraImage() {
		return this.browserWindowService.openCameraImage(this.camera, 'CAMERA', this.request)
	}

	private makeCameraStartCapture(): CameraStartCapture {
		const subFrame = this.preference.subFrame
		const x = subFrame ? this.request.x : this.camera.minX
		const y = subFrame ? this.request.y : this.camera.minY
		const width = subFrame ? this.request.width : this.camera.maxWidth
		const height = subFrame ? this.request.height : this.camera.maxHeight
		const exposureAmount =
			this.preference.exposureMode === 'LOOP' ? 0
			: this.preference.exposureMode === 'FIXED' ? this.request.exposureAmount
			: 1

		let shutterPosition = 0

		if (this.preference.wheel) {
			shutterPosition = this.preferenceService.wheel(this.preference.wheel).get().shutterPosition
		}

		Object.assign(this.request.liveStacking, this.preferenceService.settings.get().liveStacker[this.request.liveStacking.type])

		return {
			...this.request,
			shutterPosition,
			x,
			y,
			width,
			height,
			exposureAmount,
		}
	}

	protected async startCapture() {
		try {
			this.running = true
			await this.openCameraImage()
			const { mount, wheel, focuser, rotator } = this.preference
			await this.api.cameraStartCapture(this.camera, this.makeCameraStartCapture(), mount, wheel, focuser, rotator)
		} catch {
			this.running = false
		}
	}

	protected pauseCapture() {
		return this.api.cameraPauseCapture(this.camera)
	}

	protected unpauseCapture() {
		return this.api.cameraUnpauseCapture(this.camera)
	}

	protected abortCapture() {
		return this.api.cameraAbortCapture(this.camera)
	}

	private update() {
		if (this.camera.id) {
			if (this.camera.connected) {
				updateCameraStartCaptureFromCamera(this.request, this.camera)
			}
		}
	}

	protected clearSavePath() {
		this.preference.request.savePath = ''
		this.savePreference()
	}

	protected resetCameraCaptureNamingFormat(type: FrameType) {
		resetCameraCaptureNamingFormat(type, this.namingFormat.format, this.preferenceService.settings.get().namingFormat)
		this.savePreference()
	}

	protected apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.camera.id) {
			Object.assign(this.preference, this.preferenceService.camera(this.camera).get())
			this.request = this.preference.request
			this.dither.request = this.request.dither
			this.liveStacking.request = this.request.liveStacking
			this.namingFormat.format = cameraCaptureNamingFormatWithDefault(this.request.namingFormat, this.preferenceService.settings.get().namingFormat)
		}
	}

	protected savePreference() {
		if (this.mode === 'CAPTURE' && this.camera.connected) {
			Object.assign(this.preference.request, this.request)
			this.preferenceService.camera(this.camera).set(this.preference)
		}
	}

	static async showAsDialog(service: BrowserWindowService, mode: CameraMode, camera: Camera, request: CameraStartCapture) {
		const result = await service.openCameraDialog({ mode, camera, request })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
