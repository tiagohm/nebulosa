import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, Optional } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Camera, CameraCaptureState, CameraStartCapture, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureMode, ExposureTimeUnit, FrameType } from '../../shared/types/camera.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

export function cameraPreferenceKey(camera: Camera) {
    return `camera.${camera.name}`
}

export interface CameraPreference extends Partial<CameraStartCapture> {
    setpointTemperature?: number
    exposureTimeUnit?: ExposureTimeUnit
    exposureMode?: ExposureMode
    subFrame?: boolean
}

export interface CameraExposureInfo {
    count: number
    remainingTime: number
    progress: number
}

export const EMPTY_CAMERA_EXPOSURE_INFO: CameraExposureInfo = {
    count: 0,
    remainingTime: 0,
    progress: 0,
}

export interface CameraCaptureInfo {
    looping: boolean
    amount: number
    remainingTime: number
    elapsedTime: number
    progress: number
}

export const EMPTY_CAMERA_CAPTURE_INFO: CameraCaptureInfo = {
    looping: false,
    amount: 0,
    remainingTime: 0,
    elapsedTime: 0,
    progress: 0,
}

export interface CameraWaitInfo {
    remainingTime: number
    progress: number
}

export const EMPTY_CAMERA_WAIT_INFO: CameraWaitInfo = {
    remainingTime: 0,
    progress: 0,
}

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss'],
})
export class CameraComponent implements AfterContentInit, OnDestroy {

    readonly camera = Object.assign({}, EMPTY_CAMERA)

    savePath = ''
    capturesPath = ''

    wheel?: FilterWheel

    showDitherDialog = false
    dialogMode = false

    readonly cameraModel: MenuItem[] = [
        {
            icon: 'icomoon random-dither',
            label: 'Dither',
            command: () => {
                this.showDitherDialog = true
            },
        },
    ]

    hasDewHeater = false
    setpointTemperature = 0.0
    exposureTimeMin = 1
    exposureTimeMax = 1
    exposureTimeUnit = ExposureTimeUnit.MICROSECOND
    exposureMode: ExposureMode = 'SINGLE'
    subFrame = false

    readonly request = Object.assign({}, EMPTY_CAMERA_START_CAPTURE)

    state?: CameraCaptureState

    get capturing() {
        return this.state === 'EXPOSURING'
    }

    get waiting() {
        return this.state === 'WAITING'
    }

    get settling() {
        return this.state === 'SETTLING'
    }

    get running() {
        return this.capturing || this.waiting || this.settling
    }

    readonly exposure = Object.assign({}, EMPTY_CAMERA_EXPOSURE_INFO)
    readonly capture = Object.assign({}, EMPTY_CAMERA_CAPTURE_INFO)
    readonly wait = Object.assign({}, EMPTY_CAMERA_WAIT_INFO)

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

    constructor(
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
        @Optional() private app?: AppComponent,
        @Optional() private dialogRef?: DynamicDialogRef,
        @Optional() config?: DynamicDialogConfig<CameraStartCapture>,
    ) {
        if (app) app.title = 'Camera'

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.name === this.camera.name) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
                    this.update()
                })
            }
        })

        electron.on('CAMERA.DETACHED', event => {
            if (event.device.name === this.camera.name) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
                })
            }
        })

        electron.on('CAMERA.CAPTURE_ELAPSED', event => {
            if (event.camera.name === this.camera.name) {
                ngZone.run(() => {
                    this.capture.elapsedTime = event.captureElapsedTime
                    this.capture.remainingTime = event.captureRemainingTime
                    this.capture.progress = event.captureProgress
                    this.exposure.remainingTime = event.exposureRemainingTime
                    this.exposure.progress = event.exposureProgress
                    this.exposure.count = event.exposureCount

                    if (event.state === 'WAITING') {
                        this.wait.remainingTime = event.waitRemainingTime
                        this.wait.progress = event.waitProgress
                        this.state = event.state
                    } else if (event.state === 'SETTLING') {
                        this.state = event.state
                    } else if (event.state === 'CAPTURE_STARTED') {
                        this.capture.looping = event.exposureAmount <= 0
                        this.capture.amount = event.exposureAmount
                        this.state = 'EXPOSURING'
                    } else if (event.state === 'CAPTURE_FINISHED') {
                        this.state = undefined
                    } else if (event.state === 'EXPOSURE_STARTED') {
                        this.state = 'EXPOSURING'
                    }
                })
            }
        })

        this.loadCameraStartCaptureForDialogMode(config?.data)
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const camera = JSON.parse(decodeURIComponent(e.data)) as Camera
            this.cameraChanged(camera)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        if (!this.dialogMode) {
            this.abortCapture()
        }
    }

    private async loadCameraStartCaptureForDialogMode(data?: CameraStartCapture) {
        if (data) {
            Object.assign(this.request, data)
            this.dialogMode = true
            await this.cameraChanged(this.request.camera)
            this.exposureMode = 'FIXED'
            this.normalizeExposureTimeAndUnit(this.request.exposureTime)
        }
    }

    async cameraChanged(camera?: Camera) {
        if (camera && camera.name) {
            camera = await this.api.camera(camera.name)
            Object.assign(this.camera, camera)

            this.loadPreference()
            this.update()
        }

        if (this.app) {
            this.app.subTitle = camera?.name ?? ''
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
        return this.browserWindow.openCameraImage(this.camera)
    }

    openCameraCalibration() {
        return this.browserWindow.openCalibration({ data: this.camera })
    }

    private makeCameraStartCapture(): CameraStartCapture {
        const x = this.subFrame ? this.request.x : this.camera.minX
        const y = this.subFrame ? this.request.y : this.camera.minY
        const width = this.subFrame ? this.request.width : this.camera.maxWidth
        const height = this.subFrame ? this.request.height : this.camera.maxHeight
        const exposureFactor = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
        const exposureTime = Math.trunc(this.request.exposureTime * 60000000 / exposureFactor)
        const exposureAmount = this.exposureMode === 'LOOP' ? 0 : (this.exposureMode === 'FIXED' ? this.request.exposureAmount : 1)
        const savePath = this.dialogMode ? this.request.savePath : this.savePath

        return {
            ...this.request,
            camera: this.camera,
            x, y, width, height,
            exposureTime, exposureAmount,
            savePath,
        }
    }

    async startCapture() {
        await this.openCameraImage()
        this.api.cameraStartCapture(this.camera, this.makeCameraStartCapture())
    }

    abortCapture() {
        this.api.cameraAbortCapture(this.camera)
    }

    private static exposureUnitFactor(unit: ExposureTimeUnit) {
        switch (unit) {
            case ExposureTimeUnit.MINUTE: return 1
            case ExposureTimeUnit.SECOND: return 60
            case ExposureTimeUnit.MILLISECOND: return 60000
            case ExposureTimeUnit.MICROSECOND: return 60000000
        }
    }

    private updateExposureUnit(unit: ExposureTimeUnit) {
        if (this.camera.exposureMax) {
            const a = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
            const b = CameraComponent.exposureUnitFactor(unit)
            const exposureTime = Math.trunc(this.request.exposureTime * b / a)
            const exposureTimeMin = Math.trunc(this.camera.exposureMin * b / 60000000)
            const exposureTimeMax = Math.trunc(this.camera.exposureMax * b / 60000000)
            this.exposureTimeMax = Math.max(1, exposureTimeMax)
            this.exposureTimeMin = Math.max(1, exposureTimeMin)
            this.request.exposureTime = Math.max(this.exposureTimeMin, Math.min(exposureTime, this.exposureTimeMax))
            this.exposureTimeUnit = unit
        }
    }

    private normalizeExposureTimeAndUnit(exposureTime: number) {
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
                    this.updateExposureUnit(unit)
                    return
                }
            }
        }
    }

    private update() {
        if (this.camera.name) {
            if (this.camera.connected) {
                this.request.x = Math.max(this.camera.minX, Math.min(this.request.x, this.camera.maxX))
                this.request.y = Math.max(this.camera.minY, Math.min(this.request.y, this.camera.maxY))
                this.request.width = Math.max(this.camera.minWidth, Math.min(this.request.width < 8 ? this.camera.maxWidth : this.request.width, this.camera.maxWidth))
                this.request.height = Math.max(this.camera.minHeight, Math.min(this.request.height < 8 ? this.camera.maxHeight : this.request.width, this.camera.maxHeight))
                if (!this.request.frameFormat || !this.camera.frameFormats.includes(this.request.frameFormat)) this.request.frameFormat = this.camera.frameFormats[0]

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
        this.dialogRef?.close(this.makeCameraStartCapture())
    }

    private loadPreference() {
        if (!this.dialogMode && this.camera.name) {
            const preference = this.storage.get<CameraPreference>(cameraPreferenceKey(this.camera), {})

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
        }
    }

    savePreference() {
        if (!this.dialogMode && this.camera.connected) {
            const preference: CameraPreference = {
                autoSave: this.request.autoSave,
                savePath: this.savePath,
                autoSubFolderMode: this.request.autoSubFolderMode,
                setpointTemperature: this.setpointTemperature,
                exposureTime: this.request.exposureTime,
                exposureTimeUnit: this.exposureTimeUnit,
                exposureMode: this.exposureMode,
                exposureDelay: this.request.exposureDelay,
                exposureAmount: this.request.exposureAmount,
                x: this.request.x,
                y: this.request.y,
                width: this.request.width,
                height: this.request.height,
                subFrame: this.subFrame,
                binX: this.request.binX,
                binY: this.request.binY,
                frameType: this.request.frameType,
                gain: this.request.gain,
                offset: this.request.offset,
                frameFormat: this.request.frameFormat,
                dither: this.request.dither,
            }

            this.storage.set(cameraPreferenceKey(this.camera), preference)
        }
    }
}
