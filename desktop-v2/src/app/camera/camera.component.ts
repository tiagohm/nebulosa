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
    dewHeater = false
    setpointTemperature = 0.0
    exposureTime = 1
    exposureTimeUnit = ExposureTimeUnit.MICROSECOND
    exposureMode: ExposureMode = 'SINGLE'
    exposureDelay = 0
    exposureCount = 1
    x = 0.0
    y = 0.0
    width = 1023
    height = 1280
    subframe = false
    binX = 1
    binY = 1
    frameType: FrameType = 'LIGHT'
    frameFormat = ''
    gain = 0
    offset = 0

    readonly exposureModeOptions: ExposureMode[] = ['SINGLE', 'FIXED', 'LOOP']
    readonly frameTypeOptions: FrameType[] = ['LIGHT', 'DARK', 'FLAT', 'BIAS']
    readonly frameFormatOptions: string[] = []

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
        }, 5000)
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

    async update() {
        const camera = await this.api.camera(this.camera!.name)

        this.connected = camera.connected ?? false
        this.camera = camera
    }
}
