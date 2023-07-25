import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { MenuItem } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AutoSubFolderMode, Camera, CameraStartCapture, ExposureMode, ExposureTimeUnit, FrameType } from '../../shared/types'

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss']
})
export class CameraComponent implements OnInit, OnDestroy {

    cameras: Camera[] = []
    camera?: Camera
    connected = false

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
    x = 0.0
    minX = 0.0
    maxX = 0.0
    y = 0.0
    minY = 0.0
    maxY = 0.0
    width = 1023
    minWidth = 1023
    maxWidth = 1023
    height = 1280
    minHeight = 1280
    maxHeight = 1280
    subframe = false
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

    readonly exposureModeOptions: ExposureMode[] = ['SINGLE', 'FIXED', 'LOOP']
    readonly frameTypeOptions: FrameType[] = ['LIGHT', 'DARK', 'FLAT', 'BIAS']
    readonly exposureTimeUnitOptions: MenuItem[] = [
        {
            label: 'Minute (m)',
            command: () => this.updateExposureUnit(ExposureTimeUnit.MINUTE)
        },
        {
            label: 'Second (s)',
            command: () => this.updateExposureUnit(ExposureTimeUnit.SECOND)
        },
        {
            label: 'Millisecond (ms)',
            command: () => this.updateExposureUnit(ExposureTimeUnit.MILLISECOND)
        },
        {
            label: 'Microsecond (µs)',
            command: () => this.updateExposureUnit(ExposureTimeUnit.MICROSECOND)
        }
    ]

    constructor(
        private title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Camera')

        this.api.indiStartListening('CAMERA')

        electron.ipcRenderer.on('CAMERA_UPDATED', (_, data: Camera) => {
            if (data.name === this.camera?.name) {
                ngZone.run(() => {
                    this.camera = { ...data }
                    this.update()
                })
            }
        })

        electron.ipcRenderer.on('CAMERA_CAPTURE_FINISHED', (_, data: Camera) => {
            if (data.name === this.camera?.name) {
                ngZone.run(() => {
                    this.capturing = false
                })
            }
        })
    }

    async ngOnInit() {
        this.cameras = await this.api.attachedCameras()

        if (this.cameras.length > 0) {
            this.camera = this.cameras[0]
            this.update()
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('CAMERA')
    }

    async cameraSelected() {
        if (this.camera) {
            this.title.setTitle(`Camera ・ ${this.camera.name}`)
            this.camera = { ...await this.api.camera(this.camera.name) }
            this.update()
        } else {
            this.title.setTitle(`Camera`)
        }
    }

    async connect() {
        if (this.connected) {
            await this.api.cameraDisconnect(this.camera!)
        } else {
            await this.api.cameraConnect(this.camera!)
        }

        this.update()
    }

    applySetpointTemperature() {
        this.api.cameraSetpointTemperature(this.camera!, this.setpointTemperature)
    }

    toggleCooler() {
        this.api.cameraCooler(this.camera!, this.cooler)
    }

    async startCapture() {
        const x = this.subframe ? this.x : this.camera!.x
        const y = this.subframe ? this.y : this.camera!.y
        const width = this.subframe ? this.width : this.camera!.width
        const height = this.subframe ? this.height : this.camera!.height
        const exposureFactor = CameraComponent.exposureUnitFactor(this.exposureTimeUnit)
        const exposure = Math.trunc(this.exposureTime * 60000000 / exposureFactor)
        const amount = this.exposureMode === 'LOOP' ? 2147483647 :
            (this.exposureMode === 'FIXED' ? this.exposureCount : 1)

        const data: CameraStartCapture = {
            exposure, amount,
            delay: this.exposureDelay,
            x, y, width, height,
            frameFormat: this.frameFormat,
            frameType: this.frameType,
            binX: this.binX,
            binY: this.binY,
            gain: this.gain,
            offset: this.offset,
            autoSave: this.preference.get(`camera.${this.camera!.name}.autoSave`, false),
            savePath: this.preference.get(`camera.${this.camera!.name}.savePath`, ''),
            autoSubFolderMode: this.preference.get<AutoSubFolderMode>(`camera.${this.camera!.name}.autoSave`, 'OFF')
        }

        this.capturing = true

        await this.browserWindow.openCameraImage(this.camera!)

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
        if (!this.camera) {
            return
        }

        this.connected = this.camera.connected
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
        this.width = Math.max(this.minWidth, Math.min(this.width, this.maxWidth))
        this.minHeight = this.camera.minHeight
        this.maxHeight = this.camera.maxHeight
        this.height = Math.max(this.minHeight, Math.min(this.height, this.maxHeight))
        this.frameFormats = this.camera.frameFormats
        this.gainMin = this.camera.gainMin
        this.gainMax = this.camera.gainMax
        this.offsetMin = this.camera.offsetMin
        this.offsetMax = this.camera.offsetMax

        this.updateExposureUnit(this.exposureTimeUnit)
    }
}
