import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, CameraDialogInput, CameraDialogMode, CameraPreference, CameraStartCapture, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureMode, ExposureTimeUnit, FrameType, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { Equipment } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss'],
})
export class CameraComponent implements AfterContentInit, OnDestroy {

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
        return this.mode !== 'FLAT_WIZARD' && this.mode !== 'DARV'
    }

    get canExposureTimeUnit() {
        return this.mode !== 'DARV'
    }

    get canExposureAmount() {
        return this.mode === 'CAPTURE' || this.mode === 'SEQUENCER'
    }

    get canFrameType() {
        return this.mode !== 'FLAT_WIZARD' && this.mode !== 'DARV'
    }

    get canStartOrAbort() {
        return this.mode === 'CAPTURE'
    }

    get canSave() {
        return this.mode !== 'CAPTURE'
    }

    wheel?: FilterWheel

    showDitherDialog = false

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
            items: [
                {
                    icon: 'mdi mdi-telescope',
                    label: 'Mount',
                    items: [],
                },
                {
                    icon: 'mdi mdi-palette',
                    label: 'Filter Wheel',
                    items: [],
                },
                {
                    icon: 'mdi mdi-image-filter-center-focus',
                    label: 'Focuser',
                    items: [],
                },
            ]
        },
        {
            icon: 'mdi mdi-wrench',
            label: 'Calibration',
            items: [],
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

        this.cameraModel[1].visible = !app.modal
    }

    async ngAfterContentInit() {
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
    ngOnDestroy() {
        if (this.mode === 'CAPTURE') {
            this.abortCapture()
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
        if (mode === 'SEQUENCER') {
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
        const makeMountItem = (mount?: Mount) => {
            return {
                icon: mount ? 'mdi mdi-connection' : 'mdi mdi-close',
                label: mount?.name ?? 'None',
                command: async () => {
                    this.equipment.mount = mount ? await this.api.mount(mount.id) : undefined
                    this.preference.equipmentForDevice(this.camera).set(this.equipment)
                    this.electron.autoResizeWindow()
                },
            }
        }

        const mounts = await this.api.mounts()

        this.cameraModel[1].items![0].items!.push(makeMountItem())

        for (const mount of mounts) {
            this.cameraModel[1].items![0].items!.push(makeMountItem(mount))
        }

        this.equipment.mount = mounts.find(e => e.name === this.equipment.mount?.name)

        const makeWheelItem = (wheel?: FilterWheel) => {
            return {
                icon: wheel ? 'mdi mdi-connection' : 'mdi mdi-close',
                label: wheel?.name ?? 'None',
                command: async () => {
                    this.equipment.wheel = wheel ? await this.api.wheel(wheel.id) : undefined
                    this.preference.equipmentForDevice(this.camera).set(this.equipment)
                    this.electron.autoResizeWindow()
                },
            }
        }

        const wheels = await this.api.wheels()

        this.cameraModel[1].items![1].items!.push(makeWheelItem())

        for (const wheel of wheels) {
            this.cameraModel[1].items![1].items!.push(makeWheelItem(wheel))
        }

        this.equipment.wheel = wheels.find(e => e.name === this.equipment.wheel?.name)

        const makeFocuserItem = (focuser?: Focuser) => {
            return {
                icon: focuser ? 'mdi mdi-connection' : 'mdi mdi-close',
                label: focuser?.name ?? 'None',
                command: async () => {
                    this.equipment.focuser = focuser ? await this.api.focuser(focuser.id) : undefined
                    this.preference.equipmentForDevice(this.camera).set(this.equipment)
                    this.electron.autoResizeWindow()
                },
            }
        }

        const focusers = await this.api.focusers()

        this.cameraModel[1].items![2].items!.push(makeFocuserItem())

        for (const focuser of focusers) {
            this.cameraModel[1].items![2].items!.push(makeFocuserItem(focuser))
        }

        this.equipment.focuser = focusers.find(e => e.name === this.equipment.focuser?.name)

        this.electron.autoResizeWindow()
    }

    private async loadCalibrationGroups() {
        const groups = await this.api.calibrationGroups()

        const makeItem = (name?: string) => {
            return {
                label: name ?? 'None',
                icon: name ? 'mdi mdi-wrench' : 'mdi mdi-close',
                command: () => {
                    this.request.calibrationGroup = name
                },
            }
        }

        this.cameraModel[2].items!.push(makeItem())

        for (const group of groups) {
            this.cameraModel[2].items!.push(makeItem(group))
        }
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

    openCameraCalibration() {
        return this.browserWindow.openCalibration({ bringToFront: true })
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
        await this.api.cameraSnoop(this.camera, this.equipment.mount, this.equipment.wheel, this.equipment.focuser)
        await this.api.cameraStartCapture(this.camera, this.makeCameraStartCapture())
        this.preference.equipmentForDevice(this.camera).set(this.equipment)
    }

    abortCapture() {
        this.api.cameraAbortCapture(this.camera)
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
