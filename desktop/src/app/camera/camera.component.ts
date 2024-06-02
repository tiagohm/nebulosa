import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { SlideMenuItem, SlideMenuItemCommandEvent } from '../../shared/components/slide-menu/slide-menu.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, CameraDialogInput, CameraDialogMode, CameraPreference, CameraStartCapture, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureMode, ExposureTimeUnit, FrameType, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { Device } from '../../shared/types/device.types'
import { Focuser } from '../../shared/types/focuser.types'
import { Equipment } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss'],
})
export class CameraComponent implements AfterContentInit, OnDestroy, Pingable {

    readonly camera = structuredClone(EMPTY_CAMERA)
    readonly equipment: Equipment = {}

    startTooltip = ''

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

    showDitherDialog = false

    calibrationModel: MenuItem[] = []

    readonly cameraModel: MenuItem[] = [
        {
            icon: 'icomoon random-dither',
            label: 'Dither',
            command: () => {
                this.showDitherDialog = true
            },
        },
        {
            icon: 'mdi mdi-connection',
            label: 'Snoop Devices',
            subMenu: [
                {
                    icon: 'mdi mdi-telescope',
                    label: 'Mount',
                    subMenu: [],
                },
                {
                    icon: 'mdi mdi-palette',
                    label: 'Filter Wheel',
                    subMenu: [],
                },
                {
                    icon: 'mdi mdi-image-filter-center-focus',
                    label: 'Focuser',
                    subMenu: [],
                },
                {
                    icon: 'mdi mdi-rotate-right',
                    label: 'Rotator',
                    subMenu: [],
                },
            ]
        },
    ]

    hasDewHeater = false
    setpointTemperature = 0.0
    exposureTimeMin = 1
    exposureTimeMax = 1
    exposureTimeUnit = ExposureTimeUnit.MICROSECOND
    exposureMode: ExposureMode = 'SINGLE'
    subFrame = false

    readonly request = structuredClone(EMPTY_CAMERA_START_CAPTURE)
    running = false

    readonly exposureModeOptions: ExposureMode[] = ['SINGLE', 'FIXED', 'LOOP']
    readonly frameTypeOptions: FrameType[] = ['LIGHT', 'DARK', 'FLAT', 'BIAS']

    readonly exposureTimeUnitModel: MenuItem[] = [
        {
            label: 'Minute (m)',
            command: () => {
                this.updateExposureUnit(ExposureTimeUnit.MINUTE)
                this.savePreference()
            }
        },
        {
            label: 'Second (s)',
            command: () => {
                this.updateExposureUnit(ExposureTimeUnit.SECOND)
                this.savePreference()
            }
        },
        {
            label: 'Millisecond (ms)',
            command: () => {
                this.updateExposureUnit(ExposureTimeUnit.MILLISECOND)
                this.savePreference()
            }
        },
        {
            label: 'Microsecond (µs)',
            command: () => {
                this.updateExposureUnit(ExposureTimeUnit.MICROSECOND)
                this.savePreference()
            }
        }
    ]

    @ViewChild('cameraExposure')
    private readonly cameraExposure!: CameraExposureComponent

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private route: ActivatedRoute,
        private pinger: Pinger,
        ngZone: NgZone,
    ) {
        if (app) app.title = 'Camera'

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.id === this.camera.id) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
                    this.update()
                })
            }
        })

        electron.on('CAMERA.DETACHED', event => {
            if (event.device.id === this.camera.id) {
                ngZone.run(() => {
                    Object.assign(this.camera, EMPTY_CAMERA)
                })
            }
        })

        electron.on('CAMERA.CAPTURE_ELAPSED', event => {
            if (event.camera.id === this.camera.id) {
                ngZone.run(() => {
                    this.running = this.cameraExposure.handleCameraCaptureEvent(event)
                })
            }
        })

        electron.on('MOUNT.UPDATED', event => {
            if (event.device.id === this.equipment.mount?.id) {
                ngZone.run(() => {
                    Object.assign(this.equipment.mount!, event.device)
                })
            }
        })

        electron.on('WHEEL.UPDATED', event => {
            if (event.device.id === this.equipment.wheel?.id) {
                ngZone.run(() => {
                    Object.assign(this.equipment.wheel!, event.device)
                })
            }
        })

        electron.on('FOCUSER.UPDATED', event => {
            if (event.device.id === this.equipment.focuser?.id) {
                ngZone.run(() => {
                    Object.assign(this.equipment.focuser!, event.device)
                })
            }
        })

        electron.on('ROTATOR.UPDATED', event => {
            if (event.device.id === this.equipment.rotator?.id) {
                ngZone.run(() => {
                    Object.assign(this.equipment.rotator!, event.device)
                })
            }
        })

        electron.on('CALIBRATION.CHANGED', () => {
            ngZone.run(() => this.loadCalibrationGroups())
        })

        electron.on('ROI.SELECTED', event => {
            if (event.camera.id === this.camera.id) {
                ngZone.run(() => {
                    this.request.x = event.x
                    this.request.y = event.y
                    this.request.width = event.width
                    this.request.height = event.height
                })
            }
        })

        this.cameraModel[1].visible = !app.modal

        pinger.register(this, 30000)
    }

    ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const decodedData = JSON.parse(decodeURIComponent(e.data))

            if (this.app.modal) {
                this.loadCameraStartCaptureForDialogMode(decodedData)
            } else {
                this.cameraChanged(decodedData)
            }
        })

        if (!this.app.modal) {
            this.loadEquipment()
        }

        this.loadCalibrationGroups()
    }

    @HostListener('window:unload')
    async ngOnDestroy() {
        this.pinger.unregister(this)

        if (this.mode === 'CAPTURE') {
            await this.abortCapture()
        }
    }

    ping() {
        this.api.cameraListen(this.camera)
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
        if (camera && camera.id) {
            camera = await this.api.camera(camera.id)
            Object.assign(this.camera, camera)

            this.loadPreference()
            this.update()
        }

        if (this.app) {
            this.app.subTitle = camera?.name ?? ''
        }
        if (this.mode !== 'CAPTURE') {
            this.app.subTitle += ` · ${this.mode}`
        }
    }

    private async loadEquipment() {
        const buildStartTooltip = () => {
            this.startTooltip =
                `<b>MOUNT</b>: ${this.equipment.mount?.name ?? 'None'}
            <b>FILTER WHEEL</b>: ${this.equipment.wheel?.name ?? 'None'}
            <b>FOCUSER</b>: ${this.equipment.focuser?.name ?? 'None'}
            <b>ROTATOR</b>: ${this.equipment.rotator?.name ?? 'None'}`
        }

        const makeItem = (checked: boolean, command: () => void, device?: Device) => {
            return <MenuItem>{
                icon: device ? 'mdi mdi-connection' : 'mdi mdi-close',
                label: device?.name ?? 'None',
                checked,
                command: async (event: SlideMenuItemCommandEvent) => {
                    command()
                    buildStartTooltip()
                    this.preference.equipmentForDevice(this.camera).set(this.equipment)
                    event.parent?.subMenu?.forEach(item => item.checked = item === event.item)
                },
            }
        }

        // MOUNT

        const mounts = await this.api.mounts()
        this.equipment.mount = mounts.find(e => e.name === this.equipment.mount?.name)

        const makeMountItem = (mount?: Mount) => {
            return makeItem(this.equipment.mount?.name === mount?.name, () => this.equipment.mount = mount, mount)
        }

        this.cameraModel[1].subMenu![0].subMenu!.push(makeMountItem())

        for (const mount of mounts) {
            this.cameraModel[1].subMenu![0].subMenu!.push(makeMountItem(mount))
        }

        // FILTER WHEEL

        const wheels = await this.api.wheels()
        this.equipment.wheel = wheels.find(e => e.name === this.equipment.wheel?.name)

        const makeWheelItem = (wheel?: FilterWheel) => {
            return makeItem(this.equipment.wheel?.name === wheel?.name, () => this.equipment.wheel = wheel, wheel)
        }

        this.cameraModel[1].subMenu![1].subMenu!.push(makeWheelItem())

        for (const wheel of wheels) {
            this.cameraModel[1].subMenu![1].subMenu!.push(makeWheelItem(wheel))
        }

        // FOCUSER

        const focusers = await this.api.focusers()
        this.equipment.focuser = focusers.find(e => e.name === this.equipment.focuser?.name)

        const makeFocuserItem = (focuser?: Focuser) => {
            return makeItem(this.equipment.focuser?.name === focuser?.name, () => this.equipment.focuser = focuser, focuser)
        }

        this.cameraModel[1].subMenu![2].subMenu!.push(makeFocuserItem())

        for (const focuser of focusers) {
            this.cameraModel[1].subMenu![2].subMenu!.push(makeFocuserItem(focuser))
        }

        // ROTATOR

        const rotators = await this.api.rotators()
        this.equipment.rotator = rotators.find(e => e.name === this.equipment.rotator?.name)

        const makeRotatorItem = (rotator?: Rotator) => {
            return makeItem(this.equipment.rotator?.name === rotator?.name, () => this.equipment.rotator = rotator, rotator)
        }

        this.cameraModel[1].subMenu![3].subMenu!.push(makeRotatorItem())

        for (const rotator of rotators) {
            this.cameraModel[1].subMenu![3].subMenu!.push(makeRotatorItem(rotator))
        }

        buildStartTooltip()
    }

    private async loadCalibrationGroups() {
        const groups = await this.api.calibrationGroups()
        const found = !!groups.find(e => this.request.calibrationGroup === e)

        if (!found) {
            this.request.calibrationGroup = undefined
        }

        const makeItem = (name?: string) => {
            return <MenuItem>{
                label: name ?? 'None',
                icon: name ? 'mdi mdi-wrench' : 'mdi mdi-close',
                checked: this.request.calibrationGroup === name,
                command: () => {
                    this.request.calibrationGroup = name
                    this.loadCalibrationGroups()
                },
            }
        }

        const menu: SlideMenuItem[] = []

        menu.push({
            icon: 'mdi mdi-wrench',
            label: 'Open Calibration',
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
            this.api.cameraDisconnect(this.camera)
        } else {
            this.api.cameraConnect(this.camera)
        }
    }

    toggleAutoSaveAllExposures() {
        this.request.autoSave = !this.request.autoSave
        this.savePreference()
    }

    toggleAutoSubFolder() {
        switch (this.request.autoSubFolderMode) {
            case 'OFF': this.request.autoSubFolderMode = 'NOON'
                break
            case 'NOON': this.request.autoSubFolderMode = 'MIDNIGHT'
                break
            case 'MIDNIGHT': this.request.autoSubFolderMode = 'OFF'
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
        this.api.cameraSetpointTemperature(this.camera, this.setpointTemperature)
    }

    toggleCooler() {
        this.api.cameraCooler(this.camera, this.camera.cooler)
    }

    fullsize() {
        if (this.camera) {
            this.request.x = this.camera.minX
            this.request.y = this.camera.minY
            this.request.width = this.camera.maxWidth
            this.request.height = this.camera.maxHeight
            this.savePreference()
        }
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
        const exposureTime = Math.trunc(this.request.exposureTime * 60000000 / exposureFactor)
        const exposureAmount = this.exposureMode === 'LOOP' ? 0 : (this.exposureMode === 'FIXED' ? this.request.exposureAmount : 1)
        const savePath = this.mode !== 'CAPTURE' ? this.request.savePath : this.savePath

        return {
            ...this.request,
            x, y, width, height,
            exposureTime, exposureAmount,
            savePath,
        }
    }

    async startCapture() {
        await this.openCameraImage()
        await this.api.cameraSnoop(this.camera, this.equipment)
        await this.api.cameraStartCapture(this.camera, this.makeCameraStartCapture())
        this.preference.equipmentForDevice(this.camera).set(this.equipment)
    }

    abortCapture() {
        return this.api.cameraAbortCapture(this.camera)
    }

    static exposureUnitFactor(unit: ExposureTimeUnit) {
        switch (unit) {
            case ExposureTimeUnit.MINUTE: return 1
            case ExposureTimeUnit.SECOND: return 60
            case ExposureTimeUnit.MILLISECOND: return 60000
            case ExposureTimeUnit.MICROSECOND: return 60000000
        }
    }

    private updateExposureUnit(unit: ExposureTimeUnit, from: ExposureTimeUnit = this.exposureTimeUnit) {
        const exposureMax = this.camera.exposureMax || 60000000

        if (exposureMax) {
            const a = CameraComponent.exposureUnitFactor(from)
            const b = CameraComponent.exposureUnitFactor(unit)
            const exposureTime = Math.trunc(this.request.exposureTime * b / a)
            const exposureTimeMin = Math.trunc(this.camera.exposureMin * b / 60000000)
            const exposureTimeMax = Math.trunc(exposureMax * b / 60000000)
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

    apply() {
        this.app.close(this.makeCameraStartCapture())
    }

    private loadPreference() {
        if (this.mode === 'CAPTURE' && this.camera.name) {
            const preference = this.preference.cameraPreference(this.camera).get()

            this.request.autoSave = preference.autoSave ?? false
            this.savePath = preference.savePath ?? ''
            this.request.autoSubFolderMode = preference.autoSubFolderMode ?? 'OFF'
            this.setpointTemperature = preference.setpointTemperature ?? 0
            this.request.exposureTime = preference.exposureTime ?? this.camera.exposureMin
            this.exposureTimeUnit = preference.exposureTimeUnit ?? ExposureTimeUnit.MICROSECOND
            this.exposureMode = preference.exposureMode ?? 'SINGLE'
            this.request.exposureDelay = preference.exposureDelay ?? 0
            this.request.exposureAmount = preference.exposureAmount ?? 1
            this.request.x = preference.x ?? this.camera.minX
            this.request.y = preference.y ?? this.camera.minY
            this.request.width = preference.width ?? this.camera.maxWidth
            this.request.height = preference.height ?? this.camera.maxHeight
            this.subFrame = preference.subFrame ?? false
            this.request.binX = preference.binX ?? 1
            this.request.binY = preference.binY ?? 1
            this.request.frameType = preference.frameType ?? 'LIGHT'
            this.request.gain = preference.gain ?? 0
            this.request.offset = preference.offset ?? 0
            this.request.frameFormat = preference.frameFormat ?? (this.camera.frameFormats[0] || '')

            this.request.dither!.enabled = preference.dither?.enabled ?? false
            this.request.dither!.raOnly = preference.dither?.raOnly ?? false
            this.request.dither!.amount = preference.dither?.amount ?? 1.5
            this.request.dither!.afterExposures = preference.dither?.afterExposures ?? 1

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
        const result = await window.openCameraDialog({ data: { mode, camera, request } })

        if (result) {
            Object.assign(request, result)
            return true
        } else {
            return false
        }
    }
}
