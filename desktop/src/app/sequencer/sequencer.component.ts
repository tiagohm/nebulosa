import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop'
import { AfterContentInit, Component, HostListener, inject, NgZone, OnDestroy, QueryList, ViewChildren, ViewEncapsulation } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { DialogMenuComponent } from '../../shared/components/dialog-menu/dialog-menu.component'
import { MenuItem, SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { AngularService } from '../../shared/services/angular.service'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DropdownItem } from '../../shared/types/angular.types'
import { JsonFile } from '../../shared/types/app.types'
import { Camera, cameraCaptureNamingFormatWithDefault, FrameType, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { DEFAULT_SEQUENCE, DEFAULT_SEQUENCE_PROPERTY_DIALOG, DEFAULT_SEQUENCER_PLAN, DEFAULT_SEQUENCER_PREFERENCE, Sequence, SequenceProperty, SequencerEvent, SequencerPlan, sequencerPlanWithDefault } from '../../shared/types/sequencer.types'
import { resetCameraCaptureNamingFormat } from '../../shared/types/settings.types'
import { Wheel } from '../../shared/types/wheel.types'
import { deviceComparator, textComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'
import { FilterWheelComponent } from '../filterwheel/filterwheel.component'
import { RotatorComponent } from '../rotator/rotator.component'

@Component({
	selector: 'neb-sequencer',
	templateUrl: 'sequencer.component.html',
	styleUrls: ['sequencer.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class SequencerComponent implements AfterContentInit, OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly electronService = inject(ElectronService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly angularService = inject(AngularService)
	private readonly ticker = inject(Ticker)
	private readonly route = inject(ActivatedRoute)

	protected cameras: Camera[] = []
	protected mounts: Mount[] = []
	protected wheels: Wheel[] = []
	protected focusers: Focuser[] = []
	protected rotators: Rotator[] = []

	protected readonly property = structuredClone(DEFAULT_SEQUENCE_PROPERTY_DIALOG)
	protected readonly preference = structuredClone(DEFAULT_SEQUENCER_PREFERENCE)
	protected readonly calibrationGroups: DropdownItem<string | undefined>[] = []
	protected plan = this.preference.plan
	protected event?: SequencerEvent
	protected running = false
	protected path?: string

	// NOTE: Remove the "plan.sequences.length <= 1" on layout if add more options
	protected readonly sequenceModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all',
			slideMenu: [],
			command: () => {
				this.property.count = [-1000, 1000]
				this.property.showDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all above',
			slideMenu: [],
			command: () => {
				this.property.count = [-1000, 0]
				this.property.showDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to above',
			slideMenu: [],
			command: () => {
				this.property.count = [-1, 0]
				this.property.showDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to below',
			slideMenu: [],
			command: () => {
				this.property.count = [1, 0]
				this.property.showDialog = true
			},
		},
		{
			icon: 'mdi mdi-content-copy',
			label: 'Apply to all below',
			slideMenu: [],
			command: () => {
				this.property.count = [1000, 0]
				this.property.showDialog = true
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-arrow-up-bold',
			label: 'Move to top',
			slideMenu: [],
			command: () => {
				this.moveSequenceTo('TOP')
			},
		},
		{
			icon: 'mdi mdi-arrow-down-bold',
			label: 'Move to bottom',
			slideMenu: [],
			command: () => {
				this.moveSequenceTo('BOTTOM')
			},
		},
	]

	private readonly createNewMenuItem: MenuItem = {
		icon: 'mdi mdi-plus',
		label: 'Create new',
		command: () => {
			this.preference.loadPath = undefined
			this.savePreference()

			this.path = undefined
			this.updateSubTitle()

			this.saveMenuItem.visible = false
			this.saveMenuItem.disabled = true

			if (!this.loadPlan(structuredClone(DEFAULT_SEQUENCER_PLAN))) {
				this.add()
			}
		},
	}

	private readonly saveMenuItem: MenuItem = {
		icon: 'mdi mdi-content-save',
		label: 'Save',
		visible: false,
		command: () => this.savePlanToJson(false),
	}

	private readonly saveAsMenuItem: MenuItem = {
		icon: 'mdi mdi-content-save-edit',
		label: 'Save as',
		command: () => this.savePlanToJson(true),
	}

	private readonly loadMenuItem: MenuItem = {
		icon: 'mdi mdi-folder-open',
		label: 'Load',
		command: async () => {
			const defaultPath = this.preference.loadPath ? window.path.dirname(this.preference.loadPath) : undefined
			const file = await this.electronService.openJson<SequencerPlan>({ defaultPath })

			if (file !== false) {
				this.loadPlanFromJson(file)
			}
		},
	}

	@ViewChildren('cameraExposure')
	private readonly cameraExposures!: QueryList<CameraExposureComponent>

	get canStart() {
		return !!this.plan.camera?.connected && !!this.plan.sequences.find((e) => e.enabled)
	}

	get pausingOrPaused() {
		return this.event?.state === 'PAUSING' || this.event?.state === 'PAUSED'
	}

	get exposureTotal() {
		let time = 0
		let frames = 0

		for (const sequence of this.plan.sequences) {
			if (sequence.enabled) {
				time += sequence.exposureTime * sequence.exposureAmount
				frames += sequence.exposureAmount
			}
		}

		return { time, frames }
	}

	constructor() {
		const ngZone = inject(NgZone)

		this.app.title = 'Sequencer'

		this.app.topMenu.push(this.createNewMenuItem)
		this.app.topMenu.push(this.saveMenuItem)
		this.app.topMenu.push(this.saveAsMenuItem)
		this.app.topMenu.push(this.loadMenuItem)

		this.app.beforeClose = async () => {
			if (this.path && !this.saveMenuItem.disabled) {
				return !(await this.angularService.confirm('Are you sure you want to close the window? Please make sure to save before exiting to avoid losing any important changes.'))
			} else {
				return true
			}
		}

		this.electronService.on('CAMERA.UPDATED', (event) => {
			const camera = this.cameras.find((e) => e.id === event.device.id)

			if (camera) {
				ngZone.run(() => {
					Object.assign(camera, event.device)
					this.updateSequencesFromCamera(camera)
				})
			}
		})

		this.electronService.on('MOUNT.UPDATED', (event) => {
			const mount = this.mounts.find((e) => e.id === event.device.id)

			if (mount) {
				ngZone.run(() => {
					Object.assign(mount, event.device)
				})
			}
		})

		this.electronService.on('WHEEL.UPDATED', (event) => {
			const wheel = this.wheels.find((e) => e.id === event.device.id)

			if (wheel) {
				ngZone.run(() => {
					Object.assign(wheel, event.device)
				})
			}
		})

		this.electronService.on('FOCUSER.UPDATED', (event) => {
			const focuser = this.focusers.find((e) => e.id === event.device.id)

			if (focuser) {
				ngZone.run(() => {
					Object.assign(focuser, event.device)
				})
			}
		})

		this.electronService.on('ROTATOR.UPDATED', (event) => {
			const rotator = this.rotators.find((e) => e.id === event.device.id)

			if (rotator) {
				ngZone.run(() => {
					Object.assign(rotator, event.device)
				})
			}
		})

		this.electronService.on('SEQUENCER.ELAPSED', (event) => {
			ngZone.run(() => {
				if (this.running !== (event.state !== 'IDLE')) {
					this.enableOrDisableTopbarMenu(this.running)
				}

				this.event = event
				this.running = event.state !== 'IDLE'

				const captureEvent = event.capture

				if (captureEvent) {
					const index = event.id - 1
					this.cameraExposures.get(index)?.handleCameraCaptureEvent(captureEvent)
				}
			})
		})
	}

	async ngAfterContentInit() {
		this.ticker.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.mounts = (await this.api.mounts()).sort(deviceComparator)
		this.wheels = (await this.api.wheels()).sort(deviceComparator)
		this.focusers = (await this.api.focusers()).sort(deviceComparator)
		this.rotators = (await this.api.rotators()).sort(deviceComparator)

		const calibrationGroups = (await this.api.calibrationGroups()).sort(textComparator)
		this.calibrationGroups.push({ label: 'None', value: undefined })
		calibrationGroups.forEach((e) => this.calibrationGroups.push({ label: e, value: e }))

		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as Camera
			this.plan.camera = this.cameras.find((e) => e.id === data.id)
			this.updateSubTitle()
			this.loadPreference()
			await this.loadPlanFromPath()
			await this.cameraChanged()
			console.log(data, this.plan.camera)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
	}

	async tick() {
		if (this.plan.camera?.id) await this.api.cameraListen(this.plan.camera)
		if (this.plan.mount?.id) await this.api.mountListen(this.plan.mount)
		if (this.plan.focuser?.id) await this.api.focuserListen(this.plan.focuser)
		if (this.plan.wheel?.id) await this.api.wheelListen(this.plan.wheel)
		if (this.plan.rotator?.id) await this.api.rotatorListen(this.plan.rotator)
	}

	private updateSubTitle() {
		let title = ''

		if (this.plan.camera) {
			title = this.plan.camera.name
		}
		if (this.path) {
			title += ` · ${this.path}`
		}

		this.app.subTitle = title
	}

	private enableOrDisableTopbarMenu(enabled: boolean) {
		this.createNewMenuItem.disabled = !enabled
		this.loadMenuItem.disabled = !enabled
	}

	protected add() {
		const camera = this.plan.camera

		const sequence: Sequence = {
			...structuredClone(DEFAULT_SEQUENCE),
			x: camera?.minX ?? 0,
			y: camera?.minY ?? 0,
			width: camera?.maxWidth ?? 0,
			height: camera?.maxHeight ?? 0,
			frameFormat: camera?.frameFormats[0],
		}

		if (camera?.connected) {
			updateCameraStartCaptureFromCamera(sequence, camera)
		}

		this.plan.sequences.push(sequence)

		this.savePreference()
	}

	protected drop(event: CdkDragDrop<Sequence[]>) {
		if (event.previousIndex !== event.currentIndex) {
			moveItemInArray(this.plan.sequences, event.previousIndex, event.currentIndex)
			this.savePreference()
		}
	}

	private loadPlanFromJson(file: JsonFile<SequencerPlan>) {
		if (!this.loadPlan(file.json)) {
			this.angularService.message('No sequence found', 'warning')
			this.add()
		}

		this.preference.loadPath = file.path
		this.savePreference()

		this.path = file.path
		this.updateSubTitle()

		this.saveMenuItem.visible = !!file.path
		this.saveMenuItem.disabled = true
	}

	private async loadPlanFromPath() {
		if (this.preference.loadPath) {
			const file = await this.electronService.readJson<SequencerPlan>(this.preference.loadPath)

			if (file !== false && file.path) {
				this.loadPlanFromJson(file)
				return
			}

			this.angularService.message('Failed to load the file', 'danger')

			this.preference.loadPath = undefined
			this.savePreference()
		}

		this.saveMenuItem.visible = false

		if (!this.loadPlan(this.plan)) {
			this.add()
		}
	}

	private loadPlan(plan: SequencerPlan) {
		const camera = this.plan.camera

		if (this.plan !== plan) {
			Object.assign(this.plan, plan)
		}

		this.plan.camera = camera // this.cameras.find((e) => e.id === plan.camera?.id)
		this.plan.mount = this.mounts.find((e) => e.id === plan.mount?.id)
		this.plan.wheel = this.wheels.find((e) => e.id === plan.wheel?.id)
		this.plan.focuser = this.focusers.find((e) => e.id === plan.focuser?.id)
		this.plan.rotator = this.rotators.find((e) => e.id === plan.rotator?.id)

		const settings = this.preferenceService.settings.get()
		this.plan.namingFormat = cameraCaptureNamingFormatWithDefault(this.plan.namingFormat, settings.namingFormat)
		sequencerPlanWithDefault(this.plan)

		this.updateSequencesFromCamera(this.plan.camera)

		return this.plan.sequences.length
	}

	private async savePlanToJson(createNew: boolean) {
		const path = createNew ? undefined : this.preference.loadPath
		const file = await this.electronService.saveJson({ json: this.plan, path })

		if (file !== false) {
			this.preference.loadPath = file.path
			this.savePreference()

			this.path = file.path
			this.updateSubTitle()

			this.saveMenuItem.disabled = true
		}
	}

	protected resetCameraCaptureNamingFormat(type?: FrameType) {
		const settings = this.preferenceService.settings.get()
		const cameraNamingFormat = this.plan.camera?.id ? this.preferenceService.camera(this.plan.camera).get().request.namingFormat : settings.namingFormat

		if (type) {
			resetCameraCaptureNamingFormat(type, this.plan.namingFormat, cameraNamingFormat)
		} else {
			resetCameraCaptureNamingFormat('LIGHT', this.plan.namingFormat, cameraNamingFormat)
			resetCameraCaptureNamingFormat('DARK', this.plan.namingFormat, cameraNamingFormat)
			resetCameraCaptureNamingFormat('FLAT', this.plan.namingFormat, cameraNamingFormat)
			resetCameraCaptureNamingFormat('BIAS', this.plan.namingFormat, cameraNamingFormat)
		}

		this.savePreference()
	}

	protected toggleAutoSubFolder() {
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

			this.savePreference()
		}
	}

	protected async showCameraDialog(sequence: Sequence) {
		if (this.plan.camera && (await CameraComponent.showAsDialog(this.browserWindowService, 'SEQUENCER', this.plan.camera, sequence))) {
			this.savePreference()
		}
	}

	protected async showWheelDialog(sequence: Sequence) {
		if (this.plan.wheel && (await FilterWheelComponent.showAsDialog(this.browserWindowService, 'SEQUENCER', this.plan.wheel, sequence, this.plan.focuser))) {
			this.savePreference()
		}
	}

	protected async showRotatorDialog(sequence: Sequence) {
		if (this.plan.rotator && (await RotatorComponent.showAsDialog(this.browserWindowService, 'SEQUENCER', this.plan.rotator, sequence))) {
			this.savePreference()
		}
	}

	private updateSequencesFromCamera(camera?: Camera) {
		if (camera?.connected) {
			for (const sequence of this.plan.sequences) {
				updateCameraStartCaptureFromCamera(sequence, camera)
			}
		}
	}

	protected async cameraChanged() {
		if (this.plan.camera) {
			await this.api.cameraListen(this.plan.camera)
			this.updateSequencesFromCamera(this.plan.camera)
		}

		this.savePreference()
	}

	protected async mountChanged() {
		if (this.plan.mount) {
			await this.api.mountListen(this.plan.mount)
		}

		this.savePreference()
	}

	protected async focuserChanged() {
		if (this.plan.focuser) {
			await this.api.focuserListen(this.plan.focuser)
		}

		this.savePreference()
	}

	protected async wheelChanged() {
		if (this.plan.wheel) {
			await this.api.wheelListen(this.plan.wheel)
		}

		this.savePreference()
	}

	protected async rotatorChanged() {
		if (this.plan.rotator) {
			await this.api.rotatorListen(this.plan.rotator)
		}

		this.savePreference()
	}

	protected showSequenceMenu(sequence: Sequence, dialogMenu: DialogMenuComponent) {
		this.property.sequence = sequence

		const index = this.plan.sequences.indexOf(sequence)
		const lastIndex = this.plan.sequences.length - 1

		this.sequenceModel[1].visible = index >= 2 // ALL ABOBE
		this.sequenceModel[2].visible = index >= 1 // ABOBE
		this.sequenceModel[3].visible = index < lastIndex // BELOW
		this.sequenceModel[4].visible = index < lastIndex - 1 // ALL BELOW
		this.sequenceModel[0].visible = this.sequenceModel[2].visible && this.sequenceModel[3].visible

		this.sequenceModel[6].visible = this.sequenceModel[2].visible
		this.sequenceModel[7].visible = this.sequenceModel[3].visible

		if (this.sequenceModel.find((e) => e.visible)) {
			dialogMenu.show()
		}
	}

	protected selectSequenceProperty(selected: boolean) {
		for (const [key] of Object.entries(this.property.properties)) {
			this.property.properties[key as SequenceProperty] = selected
		}
	}

	protected copySequencePropertyToSequencies() {
		const source = this.property.sequence

		if (!source) return

		const index = this.plan.sequences.indexOf(source)

		for (const count of this.property.count) {
			if (index < 0 || count === 0) continue

			const below = Math.sign(count)

			for (let i = 1; i <= Math.abs(count); i++) {
				const pos = index + i * below

				if (pos >= 0 && pos < this.plan.sequences.length) {
					const dest = this.plan.sequences[pos]

					if (!dest.enabled || dest === source) continue

					if (this.property.properties.EXPOSURE_TIME) dest.exposureTime = source.exposureTime
					if (this.property.properties.EXPOSURE_AMOUNT) dest.exposureAmount = source.exposureAmount
					if (this.property.properties.EXPOSURE_DELAY) dest.exposureDelay = source.exposureDelay
					if (this.property.properties.FRAME_TYPE) dest.frameType = source.frameType
					if (this.property.properties.X) dest.x = source.x
					if (this.property.properties.Y) dest.y = source.y
					if (this.property.properties.WIDTH) dest.width = source.width
					if (this.property.properties.HEIGHT) dest.height = source.height
					if (this.property.properties.BIN) dest.binX = source.binX
					if (this.property.properties.BIN) dest.binY = source.binY
					if (this.property.properties.FRAME_FORMAT) dest.frameFormat = source.frameFormat
					if (this.property.properties.GAIN) dest.gain = source.gain
					if (this.property.properties.OFFSET) dest.offset = source.offset
					if (this.plan.liveStacking.enabled && this.property.properties.STACKING_GROUP) dest.stackerGroupType = source.stackerGroupType
					if (this.plan.liveStacking.useCalibrationGroup && this.property.properties.CALIBRATION_GROUP) dest.calibrationGroup = source.calibrationGroup
				} else {
					break
				}
			}
		}

		this.savePreference()

		this.property.showDialog = false
	}

	protected moveSequenceTo(direction: 'TOP' | 'BOTTOM') {
		const index = this.property.sequence ? this.plan.sequences.indexOf(this.property.sequence) : -1

		if (index >= 0 && this.plan.sequences.length > 1) {
			if (direction === 'TOP') {
				moveItemInArray(this.plan.sequences, index, 0)
			} else {
				moveItemInArray(this.plan.sequences, index, this.plan.sequences.length - 1)
			}

			this.savePreference()
		}
	}

	protected deleteSequence(sequence: Sequence, index: number) {
		if (sequence === this.plan.sequences[index]) {
			this.plan.sequences.splice(index, 1)
			this.savePreference()
		}
	}

	protected duplicateSequence(sequence: Sequence, index: number) {
		this.plan.sequences.splice(index + 1, 0, structuredClone(sequence))
		this.savePreference()
	}

	protected filterRemoved(sequence: Sequence) {
		sequence.filterPosition = 0
		this.savePreference()
	}

	protected angleRemoved(sequence: Sequence) {
		sequence.angle = -1
		this.savePreference()
	}

	protected async start() {
		if (this.plan.camera) {
			for (let i = 0; i < this.cameraExposures.length; i++) {
				this.cameraExposures.get(i)?.reset()
			}

			// FOCUS OFFSET
			if (this.plan.wheel && this.plan.focuser) {
				const offsets = this.preferenceService.focusOffsets(this.plan.wheel, this.plan.focuser).get()

				for (const sequence of this.plan.sequences) {
					if (sequence.filterPosition > 0) {
						sequence.focusOffset = offsets[sequence.filterPosition - 1] || 0
					}
				}
			}

			Object.assign(this.plan.liveStacking, this.preferenceService.settings.get().liveStacker[this.plan.liveStacking.type])

			await this.browserWindowService.openCameraImage(this.plan.camera, 'SEQUENCER')
			await this.api.sequencerStart(this.plan.camera, this.plan)
		}
	}

	protected async pause() {
		if (this.plan.camera) {
			await this.api.sequencerPause(this.plan.camera)
		}
	}

	protected async unpause() {
		if (this.plan.camera) {
			await this.api.sequencerUnpause(this.plan.camera)
		}
	}

	protected async stop() {
		if (this.plan.camera) {
			await this.api.sequencerStop(this.plan.camera)
		}
	}

	private loadPreference() {
		if (this.plan.camera) {
			Object.assign(this.preference, this.preferenceService.sequencer(this.plan.camera).get())
			const camera = this.plan.camera
			this.plan = this.preference.plan
			this.plan.camera = camera
			this.property.properties = this.preference.properties

			this.loadPlan(this.plan)
		}
	}

	protected savePreference() {
		if (this.plan.camera) {
			this.preferenceService.sequencer(this.plan.camera).set(this.preference)

			if (this.preference.loadPath) {
				this.saveMenuItem.disabled = false
			}
		}
	}
}
