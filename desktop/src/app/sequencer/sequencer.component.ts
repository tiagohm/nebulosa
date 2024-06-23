import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop'
import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, QueryList, ViewChildren } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { DialogMenuComponent } from '../../shared/components/dialog-menu/dialog-menu.component'
import { SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PrimeService } from '../../shared/services/prime.service'
import { JsonFile } from '../../shared/types/app.types'
import { Camera, CameraCaptureEvent, CameraStartCapture } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { EMPTY_SEQUENCE_PLAN, SEQUENCE_ENTRY_PROPERTIES, SequenceCaptureMode, SequenceEntryProperty, SequencePlan, SequencerEvent } from '../../shared/types/sequencer.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'
import { FilterWheelComponent } from '../filterwheel/filterwheel.component'

export const SEQUENCER_SAVED_PATH_KEY = 'sequencer.savedPath'
export const SEQUENCER_PLAN_KEY = 'sequencer.plan'

@Component({
	selector: 'app-sequencer',
	templateUrl: './sequencer.component.html',
	styleUrls: ['./sequencer.component.scss'],
})
export class SequencerComponent implements AfterContentInit, OnDestroy, Pingable {
	cameras: Camera[] = []
	mounts: Mount[] = []
	wheels: FilterWheel[] = []
	focusers: Focuser[] = []
	rotators: Rotator[] = []

	camera?: Camera
	mount?: Mount
	wheel?: FilterWheel
	focuser?: Focuser
	rotator?: Rotator

	readonly captureModes: SequenceCaptureMode[] = ['FULLY', 'INTERLEAVED']
	readonly plan = structuredClone(EMPTY_SEQUENCE_PLAN)

	private entryToApply?: CameraStartCapture
	private entryToApplyCount: [number, number] = [0, 0]
	readonly availableEntryPropertiesToApply = new Map<SequenceEntryProperty, boolean>()
	showEntryPropertiesToApplyDialog = false
	readonly entryMenuModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all',
			slideMenu: [],
			command: () => {
				this.entryToApplyCount = [-1000, 1000]
				this.showEntryPropertiesToApplyDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all above',
			slideMenu: [],
			command: () => {
				this.entryToApplyCount = [-1000, 0]
				this.showEntryPropertiesToApplyDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to above',
			slideMenu: [],
			command: () => {
				this.entryToApplyCount = [-1, 0]
				this.showEntryPropertiesToApplyDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to below',
			slideMenu: [],
			command: () => {
				this.entryToApplyCount = [1, 0]
				this.showEntryPropertiesToApplyDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all below',
			slideMenu: [],
			command: () => {
				this.entryToApplyCount = [1000, 0]
				this.showEntryPropertiesToApplyDialog = true
			},
		},
	]

	readonly sequenceEvents: CameraCaptureEvent[] = []

	event?: SequencerEvent
	running = false

	@ViewChildren('cameraExposure')
	private readonly cameraExposures!: QueryList<CameraExposureComponent>

	get canStart() {
		return !!this.camera && this.camera.connected && !!this.plan.entries.find((e) => e.enabled)
	}

	get savedPath() {
		return this.app.subTitle
	}

	set savedPath(value: string | undefined) {
		this.app.subTitle = value
	}

	get savedPathWasModified() {
		return !!this.app.topMenu[1].badge
	}

	set savedPathWasModified(value: boolean) {
		this.app.topMenu[1].badge = value ? '1' : undefined
	}

	constructor(
		private app: AppComponent,
		private api: ApiService,
		private browserWindow: BrowserWindowService,
		private electron: ElectronService,
		private storage: LocalStorageService,
		private prime: PrimeService,
		private pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Sequencer'

		app.topMenu.push({
			icon: 'mdi mdi-plus',
			label: 'Create new',
			command: () => {
				this.savedPath = undefined
				this.savedPathWasModified = false
				this.storage.delete(SEQUENCER_SAVED_PATH_KEY)

				Object.assign(this.plan, structuredClone(EMPTY_SEQUENCE_PLAN))
				this.add()
			},
		})
		app.topMenu.push({
			icon: 'mdi mdi-content-save',
			label: 'Save',
			command: async () => {
				const file = await electron.saveJson({ path: this.savedPath, json: this.plan })

				if (file !== false) {
					this.afterSavedJsonFile(file)
				}
			},
		})
		app.topMenu.push({
			icon: 'mdi mdi-content-save-edit',
			label: 'Save as',
			command: async () => {
				const file = await electron.saveJson({ json: this.plan })

				if (file !== false) {
					this.afterSavedJsonFile(file)
				}
			},
		})
		app.topMenu.push({
			icon: 'mdi mdi-folder-open',
			label: 'Load',
			command: async () => {
				const file = await electron.openJson<SequencePlan>()

				if (file !== false) {
					this.loadSavedJsonFile(file)
				}
			},
		})

		electron.on('CAMERA.UPDATED', (event) => {
			const camera = this.cameras.find((e) => e.id === event.device.id)

			if (camera) {
				ngZone.run(() => {
					Object.assign(camera, event.device)
				})
			}
		})

		electron.on('MOUNT.UPDATED', (event) => {
			const mount = this.mounts.find((e) => e.id === event.device.id)

			if (mount) {
				ngZone.run(() => {
					Object.assign(mount, event.device)
				})
			}
		})

		electron.on('WHEEL.UPDATED', (event) => {
			const wheel = this.wheels.find((e) => e.id === event.device.id)

			if (wheel) {
				ngZone.run(() => {
					Object.assign(wheel, event.device)
				})
			}
		})

		electron.on('FOCUSER.UPDATED', (event) => {
			const focuser = this.focusers.find((e) => e.id === event.device.id)

			if (focuser) {
				ngZone.run(() => {
					Object.assign(focuser, event.device)
				})
			}
		})

		electron.on('ROTATOR.UPDATED', (event) => {
			const rotator = this.rotators.find((e) => e.id === event.device.id)

			if (rotator) {
				ngZone.run(() => {
					Object.assign(rotator, event.device)
				})
			}
		})

		electron.on('SEQUENCER.ELAPSED', (event) => {
			ngZone.run(() => {
				if (this.running !== event.remainingTime > 0) {
					this.enableOrDisableTopbarMenu(event.remainingTime <= 0)
				}

				this.event = event
				this.running = event.remainingTime > 0

				const captureEvent = event.capture

				if (captureEvent) {
					const index = event.id - 1
					this.cameraExposures.get(index)?.handleCameraCaptureEvent(captureEvent)
				}
			})
		})

		for (const p of SEQUENCE_ENTRY_PROPERTIES) {
			this.availableEntryPropertiesToApply.set(p, true)
		}
	}

	async ngAfterContentInit() {
		this.pinger.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.mounts = (await this.api.mounts()).sort(deviceComparator)
		this.wheels = (await this.api.wheels()).sort(deviceComparator)
		this.focusers = (await this.api.focusers()).sort(deviceComparator)
		this.rotators = (await this.api.rotators()).sort(deviceComparator)

		await this.loadSavedJsonFileFromPathOrAddDefault()

		// this.route.queryParams.subscribe(e => { })
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)
	}

	async ping() {
		if (this.camera) await this.api.cameraListen(this.camera)
		if (this.mount) await this.api.mountListen(this.mount)
		if (this.focuser) await this.api.focuserListen(this.focuser)
		if (this.wheel) await this.api.wheelListen(this.wheel)
		if (this.rotator) await this.api.rotatorListen(this.rotator)
	}

	private enableOrDisableTopbarMenu(enable: boolean) {
		this.app.topMenu.forEach((e) => (e.disabled = !enable))
	}

	add() {
		const camera: Camera | undefined = this.camera ?? this.cameras[0]
		// const wheel = this.wheel ?? this.wheels[0]
		// const focuser = this.focuser ?? this.focusers[0]
		// const rotator = this.rotator ?? this.rotators[0]

		this.plan.entries.push({
			enabled: true,
			exposureTime: 1000000,
			exposureAmount: 1,
			exposureDelay: 0,
			x: camera?.minX ?? 0,
			y: camera?.minY ?? 0,
			width: camera?.maxWidth ?? 0,
			height: camera?.maxHeight ?? 0,
			frameType: 'LIGHT',
			binX: 1,
			binY: 1,
			gain: 0,
			offset: 0,
			frameFormat: camera?.frameFormats[0],
			autoSave: true,
			autoSubFolderMode: 'OFF',
			dither: {
				enabled: false,
				amount: 0,
				raOnly: false,
				afterExposures: 0,
			},
			liveStacking: {
				enabled: false,
				type: 'SIRIL',
				executablePath: '',
				use32Bits: false,
				slot: 1,
			},
		})

		this.savePlan()
	}

	drop(event: CdkDragDrop<CameraStartCapture[]>) {
		moveItemInArray(this.plan.entries, event.previousIndex, event.currentIndex)
	}

	private afterSavedJsonFile(file: JsonFile<SequencePlan>) {
		if (file.path) {
			this.savedPath = file.path
			this.storage.set(SEQUENCER_SAVED_PATH_KEY, this.savedPath)
			this.savedPathWasModified = false
		}
	}

	private loadSavedJsonFile(file: JsonFile<SequencePlan>) {
		if (this.loadPlan(file.json)) {
			this.afterSavedJsonFile(file)
		} else {
			this.prime.message(`No entry found for the saved Sequence at: ${file.path}`, 'warn')

			this.add()
		}
	}

	private async loadSavedJsonFileFromPathOrAddDefault() {
		const savedPath = this.storage.get<string | undefined>(SEQUENCER_SAVED_PATH_KEY, undefined)

		if (savedPath) {
			const file = await this.electron.readJson<SequencePlan>(savedPath)

			if (file !== false) {
				this.loadSavedJsonFile(file)
				return
			}

			this.prime.message(`Failed to load the saved Sequence at: ${savedPath}`, 'error')

			this.storage.delete(SEQUENCER_SAVED_PATH_KEY)
		}

		if (!this.loadPlan()) {
			this.add()
		}
	}

	private loadPlan(plan?: SequencePlan) {
		plan ??= this.storage.get(SEQUENCER_PLAN_KEY, this.plan)

		Object.assign(this.plan, structuredClone(plan))

		this.camera = this.cameras.find((e) => e.name === this.plan.camera?.name) ?? this.cameras[0]
		this.mount = this.mounts.find((e) => e.name === this.plan.mount?.name) ?? this.mounts[0]
		this.wheel = this.wheels.find((e) => e.name === this.plan.wheel?.name) ?? this.wheels[0]
		this.focuser = this.focusers.find((e) => e.name === this.plan.focuser?.name) ?? this.focusers[0]
		this.rotator = this.rotators.find((e) => e.name === this.plan.rotator?.name) ?? this.rotators[0]

		return plan.entries.length
	}

	toggleAutoSubFolder() {
		if (!this.running) {
			switch (this.plan.autoSubFolderMode) {
				case 'OFF':
					this.plan.autoSubFolderMode = 'NOON'
					break
				case 'NOON':
					this.plan.autoSubFolderMode = 'MIDNIGHT'
					break
				case 'MIDNIGHT':
					this.plan.autoSubFolderMode = 'OFF'
					break
			}

			this.savePlan()
		}
	}

	async showCameraDialog(entry: CameraStartCapture) {
		if (this.camera && (await CameraComponent.showAsDialog(this.browserWindow, 'SEQUENCER', this.camera, entry))) {
			this.savePlan()
		}
	}

	async showWheelDialog(entry: CameraStartCapture) {
		if (this.wheel && (await FilterWheelComponent.showAsDialog(this.browserWindow, 'SEQUENCER', this.wheel, entry))) {
			this.savePlan()
		}
	}

	cameraChanged() {
		return this.ping()
	}

	mountChanged() {
		return this.ping()
	}

	focuserChanged() {
		return this.ping()
	}

	wheelChanged() {
		return this.ping()
	}

	rotatorChanged() {
		return this.ping()
	}

	savePlan() {
		this.plan.camera = this.camera
		this.plan.mount = this.mount
		this.plan.wheel = this.wheel
		this.plan.focuser = this.focuser
		this.plan.rotator = this.rotator
		this.storage.set(SEQUENCER_PLAN_KEY, this.plan)
		this.savedPathWasModified = !!this.savedPath
	}

	showEntryMenu(entry: CameraStartCapture, dialogMenu: DialogMenuComponent) {
		this.entryToApply = entry
		const index = this.plan.entries.indexOf(entry)

		this.entryMenuModel.forEach((e) => (e.visible = true))

		if (index === 0 || this.plan.entries.length === 1) {
			// Hides all above and above.
			this.entryMenuModel[1].visible = false
			this.entryMenuModel[2].visible = false
		} else if (index === 1) {
			// Hides all above.
			this.entryMenuModel[1].visible = false
		}

		if (index === this.plan.entries.length - 1 || this.plan.entries.length === 1) {
			// Hides below and all below.
			this.entryMenuModel[3].visible = false
			this.entryMenuModel[4].visible = false
		} else if (index === this.plan.entries.length - 2) {
			// Hides all below.
			this.entryMenuModel[4].visible = false
		}

		dialogMenu.show()
	}

	updateAllAvailableEntryPropertiesToApply(selected: boolean) {
		for (const p of SEQUENCE_ENTRY_PROPERTIES) {
			this.availableEntryPropertiesToApply.set(p, selected)
		}
	}

	applyCameraStartCaptureToEntries() {
		const source = this.entryToApply
		if (!source) return
		const index = this.plan.entries.indexOf(source)

		for (let count of this.entryToApplyCount) {
			if (index < 0 || count === 0) continue

			const below = Math.sign(count)

			count = Math.abs(count)

			for (let i = 1; i <= count; i++) {
				const pos = index + i * below

				if (pos >= 0 && pos < this.plan.entries.length) {
					const dest = this.plan.entries[pos]

					if (!dest.enabled) continue

					if (this.availableEntryPropertiesToApply.get('EXPOSURE_TIME')) dest.exposureTime = source.exposureTime
					if (this.availableEntryPropertiesToApply.get('EXPOSURE_AMOUNT')) dest.exposureAmount = source.exposureAmount
					if (this.availableEntryPropertiesToApply.get('EXPOSURE_DELAY')) dest.exposureDelay = source.exposureDelay
					if (this.availableEntryPropertiesToApply.get('FRAME_TYPE')) dest.frameType = source.frameType
					if (this.availableEntryPropertiesToApply.get('X')) dest.x = source.x
					if (this.availableEntryPropertiesToApply.get('Y')) dest.y = source.y
					if (this.availableEntryPropertiesToApply.get('WIDTH')) dest.width = source.width
					if (this.availableEntryPropertiesToApply.get('HEIGHT')) dest.height = source.height
					if (this.availableEntryPropertiesToApply.get('BIN')) dest.binX = source.binX
					if (this.availableEntryPropertiesToApply.get('BIN')) dest.binY = source.binY
					if (this.availableEntryPropertiesToApply.get('FRAME_FORMAT')) dest.frameFormat = source.frameFormat
					if (this.availableEntryPropertiesToApply.get('GAIN')) dest.gain = source.gain
					if (this.availableEntryPropertiesToApply.get('OFFSET')) dest.offset = source.offset
				} else {
					break
				}
			}
		}

		this.savePlan()

		this.showEntryPropertiesToApplyDialog = false
	}

	deleteEntry(entry: CameraStartCapture, index: number) {
		if (entry === this.plan.entries[index]) {
			this.plan.entries.splice(index, 1)
			this.savePlan()
		}
	}

	duplicateEntry(entry: CameraStartCapture, index: number) {
		this.plan.entries.splice(index + 1, 0, structuredClone(entry))
		this.savePlan()
	}

	async start() {
		if (this.camera) {
			for (let i = 0; i < this.cameraExposures.length; i++) {
				this.cameraExposures.get(i)?.reset()
			}

			this.savePlan()

			await this.browserWindow.openCameraImage(this.camera, 'SEQUENCER')
			await this.api.sequencerStart(this.camera, this.plan)
		}
	}

	async stop() {
		if (this.camera) {
			await this.api.sequencerStop(this.camera)
		}
	}
}
