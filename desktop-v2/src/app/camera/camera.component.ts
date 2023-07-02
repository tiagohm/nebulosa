import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { Camera } from '../../shared/models/Camera.model'
import { ApiService } from '../../shared/services/api.service'
import { ExposureMode } from '../../shared/types/ExposureMode.type'
import { ExposureTimeUnit } from '../../shared/types/ExposureTimeUnit.type'
import { FrameType } from '../../shared/types/FrameType.type'

@Component({
    selector: 'app-camera',
    templateUrl: './camera.component.html',
    styleUrls: ['./camera.component.scss']
})
export class CameraComponent implements OnInit {

    cameras: Camera[] = []
    camera?: Camera = undefined

    cooler = false
    dewHeater = false
    setpointTemperature = 0.0
    exposureTime = 1
    exposureTimeUnit: ExposureTimeUnit = 'MICROSECOND'
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
            command: () => this.exposureTimeUnit = 'MINUTE'
        },
        {
            label: 'Second (s)',
            command: () => this.exposureTimeUnit = 'SECOND'
        },
        {
            label: 'Millisecond (ms)',
            command: () => this.exposureTimeUnit = 'MILLISECOND'
        },
        {
            label: 'Microsecond (µs)',
            command: () => this.exposureTimeUnit = 'MICROSECOND'
        }
    ]

    constructor(
        private router: Router,
        private api: ApiService,
    ) { }

    async ngOnInit() {
        this.cameras = await this.api.cameras()
    }

    exposureUnitSymbol() {
        return this.exposureTimeUnit === 'MINUTE' ? 'm' :
            this.exposureTimeUnit === 'SECOND' ? 's' :
                this.exposureTimeUnit === 'MILLISECOND' ? 'ms' : 'µs'
    }
}
