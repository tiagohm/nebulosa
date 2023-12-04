import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MegaMenuItem, MenuItem } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import {
    AutoSubFolderMode, Camera,
    CameraStartCapture, Dither, ExposureMode, ExposureTimeUnit, FilterWheel, FrameType
} from '../../shared/types'
import { AppComponent } from '../app.component'

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
            command: (e) => {
                this.autoSave = !this.autoSave
                this.savePreference()

                this.checkMenuItem(e.item, this.autoSave)
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
                    command: (e) => {
                        this.autoSubFolderMode = 'OFF'
                        this.savePreference()

                        this.checkMenu(this.cameraModel[2].items!, e.item)
                    },
                },
                {
                    icon: 'mdi mdi-weather-sunny',
                    label: 'Noon',
                    command: (e) => {
                        this.autoSubFolderMode = 'NOON'
                        this.savePreference()

                        this.checkMenu(this.cameraModel[2].items!, e.item)
                    },
                },
                {
                    icon: 'mdi mdi-weather-night',
                    label: 'Midnight',
                    command: (e) => {
                        this.autoSubFolderMode = 'MIDNIGHT'
                        this.savePreference()

                        this.checkMenu(this.cameraModel[2].items!, e.item)
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
        duration: 0,
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
        private preference: PreferenceService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Camera'

        api.startListening('CAMERA')

        electron.on('CAMERA_UPDATED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                    this.update()
                })
            }
        })

        electron.on('CAMERA_CAPTURE_STARTED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.capture.looping = event.looping
                    this.capture.amount = event.exposureAmount
                    this.capture.elapsedTime = 0
                    this.capture.remainingTime = event.estimatedTime
                    this.capture.progress = event.progress
                    this.capturing = true
                    this.waiting = false
                })
            }
        })

        electron.on('CAMERA_CAPTURE_ELAPSED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.capture.elapsedTime = event.elapsedTime
                    this.capture.remainingTime = event.remainingTime
                    this.capture.progress = event.progress
                })
            }
        })

        electron.on('CAMERA_CAPTURE_WAITING', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.wait.duration = event.waitDuration
                    this.wait.remainingTime = event.remainingTime
                    this.wait.progress = event.progress
                    this.capturing = false
                    this.waiting = true
                })
            }
        })

        electron.on('CAMERA_CAPTURE_FINISHED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.capturing = false
                    this.waiting = false
                })
            }
        })

        electron.on('CAMERA_EXPOSURE_STARTED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.exposure.remainingTime = event.remainingTime
                    this.exposure.progress = event.progress
                    this.exposure.count = event.exposureCount
                    this.capturing = true
                    this.waiting = false
                })
            }
        })

        electron.on('CAMERA_EXPOSURE_ELAPSED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.exposure.remainingTime = event.remainingTime
                    this.exposure.progress = event.progress
                    this.exposure.count = event.exposureCount
                })
            }
        })

        electron.on('CAMERA_EXPOSURE_FINISHED', event => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.exposure.remainingTime = event.remainingTime
                    this.exposure.progress = event.progress
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
        this.api.stopListening('CAMERA')
        this.abortCapture()
    }

    async cameraChanged(camera?: Camera) {
        this.camera = camera

        if (this.camera) {
            this.app.subTitle = this.camera.name

            const camera = await this.api.camera(this.camera.name)
            Object.assign(this.camera, camera)

            await this.loadPreference()
            this.update()

            this.preference.set('camera.selected', this.camera.name)
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

    resetSavePath() {
        this.savePath = ''
        this.preference.set(`camera.${this.camera!.name}.savePath`, this.savePath)
    }

    private async loadPreference() {
        if (this.camera) {
            this.autoSave = await this.preference.get(`camera.${this.camera.name}.autoSave`, false)
            this.savePath = await this.preference.get(`camera.${this.camera.name}.savePath`, '')
            this.autoSubFolderMode = await this.preference.get<AutoSubFolderMode>(`camera.${this.camera.name}.autoSubFolderMode`, 'OFF')

            this.checkMenuItem(this.cameraModel[0], this.autoSave)
            const menuIndex = this.autoSubFolderMode === 'OFF' ? 0 : (this.autoSubFolderMode === 'NOON' ? 1 : 2)
            this.checkMenu(this.cameraModel[2].items!, this.cameraModel[2].items![menuIndex], true)

            this.setpointTemperature = await this.preference.get(`camera.${this.camera.name}.setpointTemperature`, 0)
            this.exposureTime = await this.preference.get(`camera.${this.camera.name}.exposureTime`, this.camera.exposureMin)
            this.exposureTimeUnit = await this.preference.get(`camera.${this.camera.name}.exposureTimeUnit`, ExposureTimeUnit.MICROSECOND)
            this.exposureMode = await this.preference.get(`camera.${this.camera.name}.exposureMode`, 'SINGLE')
            this.exposureDelay = await this.preference.get(`camera.${this.camera.name}.exposureDelay`, 0)
            this.exposureCount = await this.preference.get(`camera.${this.camera.name}.exposureCount`, 1)
            this.x = await this.preference.get(`camera.${this.camera.name}.x`, this.camera.minX)
            this.y = await this.preference.get(`camera.${this.camera.name}.y`, this.camera.minY)
            this.width = await this.preference.get(`camera.${this.camera.name}.width`, this.camera.maxWidth)
            this.height = await this.preference.get(`camera.${this.camera.name}.height`, this.camera.maxHeight)
            this.subFrame = await this.preference.get(`camera.${this.camera.name}.subFrame`, false)
            this.binX = await this.preference.get(`camera.${this.camera.name}.binX`, 1)
            this.binY = await this.preference.get(`camera.${this.camera.name}.binY`, 1)
            this.frameType = await this.preference.get(`camera.${this.camera.name}.frameType`, 'LIGHT')
            this.gain = await this.preference.get(`camera.${this.camera.name}.gain`, 0)
            this.offset = await this.preference.get(`camera.${this.camera.name}.offset`, 0)
            this.frameFormat = await this.preference.get(`camera.${this.camera.name}.frameFormat`, this.camera.frameFormats[0] || '')

            this.dithering.enabled = await this.preference.get(`camera.${this.camera.name}.dithering.enabled`, false)
            this.dithering.raOnly = await this.preference.get(`camera.${this.camera.name}.dithering.raOnly`, false)
            this.dithering.amount = await this.preference.get(`camera.${this.camera.name}.dithering.amount`, 1.5)
            this.dithering.afterExposures = await this.preference.get(`camera.${this.camera.name}.dithering.afterExposures`, 1)
        }
    }

    savePreference() {
        if (this.camera && this.camera.connected) {
            this.preference.set(`camera.${this.camera.name}.autoSave`, this.autoSave)
            this.preference.set(`camera.${this.camera.name}.savePath`, this.savePath)
            this.preference.set(`camera.${this.camera.name}.autoSubFolderMode`, this.autoSubFolderMode)
            this.preference.set(`camera.${this.camera.name}.setpointTemperature`, this.setpointTemperature)
            this.preference.set(`camera.${this.camera.name}.exposureTime`, this.exposureTime)
            this.preference.set(`camera.${this.camera.name}.exposureTimeUnit`, this.exposureTimeUnit)
            this.preference.set(`camera.${this.camera.name}.exposureMode`, this.exposureMode)
            this.preference.set(`camera.${this.camera.name}.exposureDelay`, this.exposureDelay)
            this.preference.set(`camera.${this.camera.name}.exposureCount`, this.exposureCount)
            this.preference.set(`camera.${this.camera.name}.x`, this.x)
            this.preference.set(`camera.${this.camera.name}.y`, this.y)
            this.preference.set(`camera.${this.camera.name}.width`, this.width)
            this.preference.set(`camera.${this.camera.name}.height`, this.height)
            this.preference.set(`camera.${this.camera.name}.subFrame`, this.subFrame)
            this.preference.set(`camera.${this.camera.name}.binX`, this.binX)
            this.preference.set(`camera.${this.camera.name}.binY`, this.binY)
            this.preference.set(`camera.${this.camera.name}.frameType`, this.frameType)
            this.preference.set(`camera.${this.camera.name}.gain`, this.gain)
            this.preference.set(`camera.${this.camera.name}.offset`, this.offset)
            this.preference.set(`camera.${this.camera.name}.frameFormat`, this.frameFormat)

            this.preference.set(`camera.${this.camera.name}.dithering.enabled`, this.dithering.enabled)
            this.preference.set(`camera.${this.camera.name}.dithering.raOnly`, this.dithering.raOnly)
            this.preference.set(`camera.${this.camera.name}.dithering.amount`, this.dithering.amount)
            this.preference.set(`camera.${this.camera.name}.dithering.afterExposures`, this.dithering.afterExposures)
        }
    }

    private checkMenuItem(item?: MenuItem | MegaMenuItem, checked: boolean = true) {
        item && (item.styleClass = checked ? 'p-menuitem-checked' : '')
    }

    private checkMenu(menu: MenuItem[], item?: MenuItem | MegaMenuItem, checked: boolean = true) {
        menu.forEach((e) => e !== item && this.checkMenuItem(e, false))
        this.checkMenuItem(item, checked)
    }
}
