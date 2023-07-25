import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { HipsSurvey } from '../../shared/types'

export interface FramingParams {
    rightAscension: string
    declination: string
    width?: number
    height?: number
    fov?: number
    rotation?: number
}

@Component({
    selector: 'app-framing',
    templateUrl: './framing.component.html',
    styleUrls: ['./framing.component.scss'],
})
export class FramingComponent implements OnInit, OnDestroy {

    rightAscension = '00h00m00s'
    declination = `+000°00'00"`
    width = 1280
    height = 720
    fov = 1.0
    rotation = 0.0
    hipsSurveys: HipsSurvey[] = []
    hipsSurvey?: HipsSurvey

    loading = false

    private framePath = ''
    private frameId = ''

    constructor(
        title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Framing')

        electron.ipcRenderer.on('PARAMS_CHANGED', (_, data: FramingParams) => {
            ngZone.run(() => {
                this.rightAscension = data.rightAscension
                this.declination = data.declination
                this.width = data.width ?? this.width
                this.height = data.height ?? this.height
                this.fov = data.fov ?? this.fov
                if (data.rotation === 0 || data.rotation) this.rotation = data.rotation

                this.frame()
            })
        })
    }

    async ngOnInit() {
        this.hipsSurveys = await this.api.hipsSurveys()

        this.rightAscension = this.preference.get('framing.rightAscension', '00h00m00s')
        this.declination = this.preference.get('framing.declination', `+000°00'00"`)
        this.width = this.preference.get('framing.width', 1280)
        this.height = this.preference.get('framing.height', 720)
        this.fov = this.preference.get('framing.fov', 1)
        this.rotation = this.preference.get('framing.rotation', 0)
        const id = this.preference.get('framing.hipsSurvey', 'CDS/P/DSS2/color')

        const hipsSurvey = this.hipsSurveys.find(e => e.id === id) ?? this.hipsSurveys[0]
        setTimeout(() => this.hipsSurvey = hipsSurvey, 600)
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.closeFrameImage()
        this.electron.ipcRenderer.sendSync('close-window', this.frameId)
    }

    async frame() {
        await this.closeFrameImage()

        this.loading = true

        try {
            const path = await this.api.frame(this.rightAscension, this.declination, this.width, this.height, this.fov, this.rotation, this.hipsSurvey!)
            const title = `Framing ・ ${this.rightAscension} ・ ${this.declination}`
            this.frameId = await this.browserWindow.openImage(path, 'framing', 'FRAMING', title)
            this.framePath = path

            this.preference.set('framing.rightAscension', this.rightAscension)
            this.preference.set('framing.declination', this.declination)
            this.preference.set('framing.width', this.width)
            this.preference.set('framing.height', this.height)
            this.preference.set('framing.fov', this.fov)
            this.preference.set('framing.rotation', this.rotation)
            this.preference.set('framing.hipsSurvey', this.hipsSurvey!.id)
        } finally {
            this.loading = false
        }
    }

    private async closeFrameImage() {
        if (this.framePath) {
            await this.api.closeImage(this.framePath)
        }
    }
}