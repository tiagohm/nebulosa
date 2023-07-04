import { Component, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { Router } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { ExposureTimeUnit } from '../../shared/enums/ExposureTimeUnit.enum'
import { Camera } from '../../shared/models/Camera.model'
import { ApiService } from '../../shared/services/api.service'
import { ExposureMode } from '../../shared/types/ExposureMode.type'
import { FrameType } from '../../shared/types/FrameType.type'

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss']
})
export class CameraComponent implements OnInit, OnDestroy {

    cameras: Camera[] = []
    camera?: Camera = undefined
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

    readonly exposureModeOptions: ExposureMode[] = ['SINGLE', 'FIXED', 'LOOP']
    readonly frameTypeOptions: FrameType[] = ['LIGHT', 'DARK', 'FLAT', 'BIAS']
    readonly exposureTimeUnitOptions: MenuItem[] = [
        {
            label: 'Minute (m)',
            command: () => this.exposureTimeUnit = ExposureTimeUnit.MINUTE
        },
        {
            label: 'Second (s)',
            command: () => this.exposureTimeUnit = ExposureTimeUnit.SECOND
        },
        {
            label: 'Millisecond (ms)',
            command: () => this.exposureTimeUnit = ExposureTimeUnit.MILLISECOND
        },
        {
            label: 'Microsecond (µs)',
            command: () => this.exposureTimeUnit = ExposureTimeUnit.MICROSECOND
        }
    ]

    private timer?: any = undefined
    private refreshing = false

    constructor(
        private router: Router,
        private api: ApiService,
        title: Title,
    ) {
        title.setTitle('Camera')
    }

    async ngOnInit() {
        this.cameras = await this.api.cameras()

        if (this.cameras.length > 0) {
            this.camera = this.cameras[0]
            this.update()
        }

        this.timer = setInterval(() => {
            this.update()
        }, 2500)
    }

    ngOnDestroy() {
        clearInterval(this.timer)
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
        this.api.setpointTemperature(this.camera!, this.setpointTemperature)
    }

    switchCooler() {
        this.api.cooler(this.camera!, this.cooler)
    }

    private async update() {
        if (!this.camera) {
            this.connected = false
            return
        }

        if (this.refreshing) {
            return
        }

        this.refreshing = true

        const camera = await this.api.camera(this.camera!.name)

        this.refreshing = false

        this.connected = camera.connected
        this.cooler = camera.cooler
        this.hasCooler = camera.hasCooler
        this.coolerPower = camera.coolerPower
        this.dewHeater = camera.dewHeater
        this.temperature = camera.temperature
        this.canSetTemperature = camera.canSetTemperature
        this.minX = camera.minX
        this.maxX = camera.maxX
        this.x = Math.max(this.minX, Math.min(this.x, this.maxX))
        this.minY = camera.minY
        this.maxY = camera.maxY
        this.y = Math.max(this.minY, Math.min(this.y, this.maxY))
        this.minWidth = camera.minWidth
        this.maxWidth = camera.maxWidth
        this.width = Math.max(this.minWidth, Math.min(this.width, this.maxWidth))
        this.minHeight = camera.minHeight
        this.maxHeight = camera.maxHeight
        this.height = Math.max(this.minHeight, Math.min(this.height, this.maxHeight))
        this.frameFormats = camera.frameFormats
        this.gainMin = camera.gainMin
        this.gainMax = camera.gainMax
        this.offsetMin = camera.offsetMin
        this.offsetMax = camera.offsetMax

        this.camera = camera

        this.updateExposureTime()
    }

    private updateExposureTime() {
        this.exposureTimeMin = this.camera!.exposureMin
        this.exposureTimeMax = this.camera!.exposureMax
    }
}
