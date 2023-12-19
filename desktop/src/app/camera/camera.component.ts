import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, Optional } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Camera, CameraCaptureState, CameraStartCapture, EMPTY_CAMERA, ExposureMode, ExposureTimeUnit, FilterWheel, FrameType } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface CameraPreference extends Partial<CameraStartCapture> {
    setpointTemperature?: number
    exposureTimeUnit?: ExposureTimeUnit
    exposureMode?: ExposureMode
    subFrame?: boolean
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
            icon: 'mdi mdi-content-save',
            label: 'Auto save all exposures',
            command: () => {
                this.request.autoSave = !this.request.autoSave
                this.savePreference()
            },
        },
        {
            icon: 'mdi mdi-folder',
            label: 'Save path...',
            command: async () => {
                const defaultPath = this.savePath || this.capturesPath
                const path = await this.electron.openDirectory({ defaultPath })

                if (path) {
                    this.savePath = path
                    this.savePreference()
                }
            },
        },
        {
            icon: 'mdi mdi-folder-plus',
            label: 'New sub folder at',
            items: [
                {
                    icon: 'mdi mdi-folder-off',
                    label: 'None',
                    command: () => {
                        this.request.autoSubFolderMode = 'OFF'
                        this.savePreference()
                    },
                },
                {
                    icon: 'mdi mdi-weather-sunny',
                    label: 'Noon',
                    command: () => {
                        this.request.autoSubFolderMode = 'NOON'
                        this.savePreference()
                    },
                },
                {
                    icon: 'mdi mdi-weather-night',
                    label: 'Midnight',
                    command: () => {
                        this.request.autoSubFolderMode = 'MIDNIGHT'
                        this.savePreference()
                    },
                },
            ],
        },
        {
            separator: true,
        },
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

    readonly request: CameraStartCapture = {
        exposureTime: 1,
        exposureAmount: 1,
        exposureDelay: 0,
        x: 0,
        y: 0,
        width: 0,
        height: 0,
        frameType: 'LIGHT',
        binX: 1,
        binY: 1,
        gain: 0,
        offset: 0,
        autoSave: false,
        autoSubFolderMode: 'OFF',
        dither: {
            enabled: false,
            afterExposures: 1,
            amount: 1.5,
            raOnly: false,
        }
    }

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

    readonly exposure = {
        count: 0,
        remainingTime: 0,
        progress: 0,
    }

    readonly capture = {
        looping: false,
        amount: 0,
        remainingTime: 0,
        elapsedTime: 0,
        progress: 0,
    }

    readonly wait = {
        remainingTime: 0,
        progress: 0,
    }

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

        electron.on('CAMERA_UPDATED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                    this.update()
                })
            }
        })

        electron.on('CAMERA_DETACHED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                })
            }
        })

        electron.on('CAMERA_CAPTURE_ELAPSED', event => {
            if (event.camera.name === this.camera?.name) {
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

        if (config) {
            Object.assign(this.request, config.data)
            this.dialogMode = true
            this.cameraChanged(this.request.camera)
        }
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
        if (this.camera!.connected) {
            this.api.cameraDisconnect(this.camera!)
        } else {
            this.api.cameraConnect(this.camera!)
        }
    }

    applySetpointTemperature() {
        this.savePreference()
        this.api.cameraSetpointTemperature(this.camera!, this.setpointTemperature)
    }

    toggleCooler() {
        this.api.cameraCooler(this.camera!, this.camera!.cooler)
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
        return this.browserWindow.openCameraImage(this.camera!)
    }

    openCameraCalibration() {
        return this.browserWindow.openCalibration({ data: this.camera! })
    }

    private makeCameraStartCapture(): CameraStartCapture {
        const x = this.subFrame ? this.request.x : this.camera!.minX
        const y = this.subFrame ? this.request.y : this.camera!.minY
        const width = this.subFrame ? this.request.width : this.camera!.maxWidth
        const height = this.subFrame ? this.request.height : this.camera!.maxHeight
        const exposureFactor = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
        const exposureTime = Math.trunc(this.request.exposureTime * 60000000 / exposureFactor)
        const exposureAmount = this.exposureMode === 'LOOP' ? 0 : (this.exposureMode === 'FIXED' ? this.request.exposureAmount : 1)

        return {
            ...this.request,
            exposureDelay: this.request.exposureDelay * 1000000,
            x, y, width, height,
            exposureTime, exposureAmount,
        }
    }

    async startCapture() {
        await this.openCameraImage()
        this.api.cameraStartCapture(this.camera!, this.makeCameraStartCapture())
    }

    abortCapture() {
        this.api.cameraAbortCapture(this.camera!)
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
        if (this.camera!.exposureMax) {
            const a = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
            const b = CameraComponent.exposureUnitFactor(unit)
            const exposureTime = Math.trunc(this.request.exposureTime * b / a)
            const exposureTimeMin = Math.trunc(this.camera!.exposureMin * b / 60000000)
            const exposureTimeMax = Math.trunc(this.camera!.exposureMax * b / 60000000)
            this.exposureTimeMax = Math.max(1, exposureTimeMax)
            this.exposureTimeMin = Math.max(1, exposureTimeMin)
            this.request.exposureTime = Math.max(this.exposureTimeMin, Math.min(exposureTime, this.exposureTimeMax))
            this.exposureTimeUnit = unit
        }
    }

    private update() {
        if (this.camera) {
            if (this.camera.connected) {
                this.request.x = Math.max(this.camera.minX, Math.min(this.request.x, this.camera.maxX))
                this.request.y = Math.max(this.camera.minY, Math.min(this.request.y, this.camera.maxY))
                this.request.width = Math.max(this.camera.minWidth, Math.min(this.request.width < 8 ? this.camera.maxWidth : this.request.width, this.camera.maxWidth))
                this.request.height = Math.max(this.camera.minHeight, Math.min(this.request.height < 8 ? this.camera.maxHeight : this.request.width, this.camera.maxHeight))
                if (!this.request.frameFormat) this.request.frameFormat = this.camera.frameFormats[0]

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
        if (this.camera) {
            const mode = this.dialogMode ? '.dialog' : ''
            const preference = this.storage.get<CameraPreference>(`camera.${this.camera.name}${mode}`, {})

            this.request.autoSave = preference.autoSave ?? false
            this.savePath = preference.savePath ?? ''
            this.request.autoSubFolderMode = preference.autoSubFolderMode ?? 'OFF'
            this.setpointTemperature = preference.setpointTemperature ?? 0
            this.request.exposureTime = preference.exposureTime ?? this.camera.exposureMin
            this.exposureTimeUnit = preference.exposureTimeUnit ?? ExposureTimeUnit.MICROSECOND
            this.exposureMode = this.dialogMode ? 'FIXED' : (preference.exposureMode ?? 'SINGLE')
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
        if (this.camera && this.camera.connected) {
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

            const mode = this.dialogMode ? '.dialog' : ''
            this.storage.set(`camera.${this.camera.name}${mode}`, preference)
        }
    }
}
