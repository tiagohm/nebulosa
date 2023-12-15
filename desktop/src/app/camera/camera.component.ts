import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { AutoSubFolderMode, Camera, CameraStartCapture, Dither, ExposureMode, ExposureTimeUnit, FilterWheel, FrameType } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface CameraPreference {
    autoSave?: boolean
    savePath?: string
    autoSubFolderMode?: AutoSubFolderMode
    setpointTemperature?: number
    exposureTime?: number
    exposureTimeUnit?: ExposureTimeUnit
    exposureMode?: ExposureMode
    exposureDelay?: number
    exposureCount?: number
    x?: number
    y?: number
    width?: number
    height?: number
    subFrame?: boolean
    binX?: number
    binY?: number
    frameType?: FrameType
    gain?: number
    offset?: number
    frameFormat?: string
    dithering?: Dither
}

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss'],
})
export class CameraComponent implements AfterContentInit, OnDestroy {

    camera?: Camera
    connected = false

    autoSave = false
    savePath = ''
    capturesPath = ''
    autoSubFolderMode: AutoSubFolderMode = 'OFF'

    wheel?: FilterWheel

    showDitheringDialog = false
    readonly dithering: Dither = {
        enabled: false,
        afterExposures: 1,
        amount: 1.5,
        raOnly: false,
    }

    readonly cameraModel: MenuItem[] = [
        {
            icon: 'mdi mdi-content-save',
            label: 'Auto save all exposures',
            command: () => {
                this.autoSave = !this.autoSave
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
                        this.autoSubFolderMode = 'OFF'
                        this.savePreference()
                    },
                },
                {
                    icon: 'mdi mdi-weather-sunny',
                    label: 'Noon',
                    command: () => {
                        this.autoSubFolderMode = 'NOON'
                        this.savePreference()
                    },
                },
                {
                    icon: 'mdi mdi-weather-night',
                    label: 'Midnight',
                    command: () => {
                        this.autoSubFolderMode = 'MIDNIGHT'
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
            label: 'Dithering',
            command: () => {
                this.showDitheringDialog = true
            },
        },
    ]

    cooler = false
    hasCooler = false
    coolerPower = 0.0
    dewHeater = false
    hasDewHeater = false
    temperature = 0.0
    canSetTemperature = false
    setpointTemperature = 0.0
    exposureTime = 1
    exposureTimeMin = 1
    exposureTimeMax = 1
    exposureTimeUnit = ExposureTimeUnit.MICROSECOND
    exposureMode: ExposureMode = 'SINGLE'
    exposureDelay = 0
    exposureCount = 1
    x = 0
    minX = 0
    maxX = 0
    y = 0
    minY = 0
    maxY = 0
    width = 1023
    minWidth = 1023
    maxWidth = 1023
    height = 1280
    minHeight = 1280
    maxHeight = 1280
    subFrame = false
    binX = 1
    binY = 1
    frameType: FrameType = 'LIGHT'
    frameFormats: string[] = []
    frameFormat = ''
    gain = 0
    gainMin = 0
    gainMax = 0
    offset = 0
    offsetMin = 0
    offsetMax = 0

    capturing = false
    waiting = false

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
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Camera'

        electron.on('CAMERA_UPDATED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                    this.update()
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
                        this.waiting = true
                    } else if (event.state === 'CAPTURE_STARTED') {
                        this.capture.looping = event.exposureAmount <= 0
                        this.capture.amount = event.exposureAmount
                        this.capturing = true
                    } else if (event.state === 'CAPTURE_FINISHED') {
                        this.capturing = false
                        this.waiting = false
                    } else if (event.state === 'EXPOSURE_STARTED') {
                        this.waiting = false
                    }
                })
            }
        })
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const camera = JSON.parse(decodeURIComponent(e.data)) as Camera
            this.cameraChanged(camera)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.abortCapture()
    }

    async cameraChanged(camera?: Camera) {
        this.camera = camera

        if (this.camera) {
            this.app.subTitle = this.camera.name

            const camera = await this.api.camera(this.camera.name)
            Object.assign(this.camera, camera)

            this.loadPreference()
            this.update()
        } else {
            this.app.subTitle = ''
        }
    }

    connect() {
        if (this.connected) {
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
        this.api.cameraCooler(this.camera!, this.cooler)
    }

    fullsize() {
        if (this.camera) {
            this.x = this.camera.minX
            this.y = this.camera.minY
            this.width = this.camera.maxWidth
            this.height = this.camera.maxHeight
            this.savePreference()
        }
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera!)
    }

    openCameraCalibration() {
        return this.browserWindow.openCalibration({ data: this.camera! })
    }

    async startCapture() {
        const x = this.subFrame ? this.x : this.camera!.minX
        const y = this.subFrame ? this.y : this.camera!.minY
        const width = this.subFrame ? this.width : this.camera!.maxWidth
        const height = this.subFrame ? this.height : this.camera!.maxHeight
        const exposureFactor = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
        const exposureTime = Math.trunc(this.exposureTime * 60000000 / exposureFactor)
        const exposureAmount = this.exposureMode === 'LOOP' ? 0 : (this.exposureMode === 'FIXED' ? this.exposureCount : 1)

        const data: CameraStartCapture = {
            exposureTime, exposureAmount,
            exposureDelay: this.exposureDelay * 1000000,
            x, y, width, height,
            frameFormat: this.frameFormat,
            frameType: this.frameType,
            binX: this.binX,
            binY: this.binY,
            gain: this.gain,
            offset: this.offset,
            autoSave: this.autoSave,
            savePath: this.savePath,
            autoSubFolderMode: this.autoSubFolderMode,
            dither: this.dithering,
        }

        await this.openCameraImage()

        this.api.cameraStartCapture(this.camera!, data)
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
        const a = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
        const b = CameraComponent.exposureUnitFactor(unit)
        const exposureTime = Math.trunc(this.exposureTime * b / a)
        const exposureTimeMin = Math.trunc(this.camera!.exposureMin * b / 60000000)
        const exposureTimeMax = Math.trunc(this.camera!.exposureMax * b / 60000000)
        this.exposureTimeMax = Math.max(1, exposureTimeMax)
        this.exposureTimeMin = Math.max(1, exposureTimeMin)
        this.exposureTime = Math.max(this.exposureTimeMin, Math.min(exposureTime, this.exposureTimeMax))
        this.exposureTimeUnit = unit
    }

    private async update() {
        if (this.camera) {
            this.connected = this.camera.connected

            if (this.connected) {
                this.cooler = this.camera.cooler
                this.hasCooler = this.camera.hasCooler
                this.coolerPower = this.camera.coolerPower
                this.dewHeater = this.camera.dewHeater
                this.temperature = this.camera.temperature
                this.canSetTemperature = this.camera.canSetTemperature
                this.minX = this.camera.minX
                this.maxX = this.camera.maxX
                this.x = Math.max(this.minX, Math.min(this.x, this.maxX))
                this.minY = this.camera.minY
                this.maxY = this.camera.maxY
                this.y = Math.max(this.minY, Math.min(this.y, this.maxY))
                this.minWidth = this.camera.minWidth
                this.maxWidth = this.camera.maxWidth
                this.width = Math.max(this.minWidth, Math.min(this.width < 8 ? this.maxWidth : this.width, this.maxWidth))
                this.minHeight = this.camera.minHeight
                this.maxHeight = this.camera.maxHeight
                this.height = Math.max(this.minHeight, Math.min(this.height < 8 ? this.maxHeight : this.width, this.maxHeight))
                this.frameFormats = this.camera.frameFormats
                if (!this.frameFormat) this.frameFormat = this.frameFormats[0]
                this.gainMin = this.camera.gainMin
                this.gainMax = this.camera.gainMax
                this.offsetMin = this.camera.offsetMin
                this.offsetMax = this.camera.offsetMax

                this.updateExposureUnit(this.exposureTimeUnit)
            }

            this.capturesPath = this.camera.capturesPath
        }
    }

    clearSavePath() {
        this.savePath = ''
        this.savePreference()
    }

    private loadPreference() {
        if (this.camera) {
            const preference = this.storage.get<CameraPreference>(`camera.${this.camera.name}`, {})
            this.autoSave = preference.autoSave ?? false
            this.savePath = preference.savePath ?? ''
            this.autoSubFolderMode = preference.autoSubFolderMode ?? 'OFF'
            this.setpointTemperature = preference.setpointTemperature ?? 0
            this.exposureTime = preference.exposureTime ?? this.camera.exposureMin
            this.exposureTimeUnit = preference.exposureTimeUnit ?? ExposureTimeUnit.MICROSECOND
            this.exposureMode = preference.exposureMode ?? 'SINGLE'
            this.exposureDelay = preference.exposureDelay ?? 0
            this.exposureCount = preference.exposureCount ?? 1
            this.x = preference.x ?? this.camera.minX
            this.y = preference.y ?? this.camera.minY
            this.width = preference.width ?? this.camera.maxWidth
            this.height = preference.height ?? this.camera.maxHeight
            this.subFrame = preference.subFrame ?? false
            this.binX = preference.binX ?? 1
            this.binY = preference.binY ?? 1
            this.frameType = preference.frameType ?? 'LIGHT'
            this.gain = preference.gain ?? 0
            this.offset = preference.offset ?? 0
            this.frameFormat = preference.frameFormat ?? (this.camera.frameFormats[0] || '')

            this.dithering.enabled = preference.dithering?.enabled ?? false
            this.dithering.raOnly = preference.dithering?.raOnly ?? false
            this.dithering.amount = preference.dithering?.amount ?? 1.5
            this.dithering.afterExposures = preference.dithering?.afterExposures ?? 1
        }
    }

    savePreference() {
        if (this.camera && this.camera.connected) {
            const preference: CameraPreference = {
                autoSave: this.autoSave,
                savePath: this.savePath,
                autoSubFolderMode: this.autoSubFolderMode,
                setpointTemperature: this.setpointTemperature,
                exposureTime: this.exposureTime,
                exposureTimeUnit: this.exposureTimeUnit,
                exposureMode: this.exposureMode,
                exposureDelay: this.exposureDelay,
                exposureCount: this.exposureCount,
                x: this.x,
                y: this.y,
                width: this.width,
                height: this.height,
                subFrame: this.subFrame,
                binX: this.binX,
                binY: this.binY,
                frameType: this.frameType,
                gain: this.gain,
                offset: this.offset,
                frameFormat: this.frameFormat,
                dithering: this.dithering,
            }

            this.storage.set(`camera.${this.camera.name}`, preference)
        }
    }
}
