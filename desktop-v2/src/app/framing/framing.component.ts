import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
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
        ngZone: NgZone,
    ) {
        title.setTitle('Framing')

        electron.ipcRenderer.on('PARAMS_CHANGED', (_, data: FramingParams) => {
            this.rightAscension = data.rightAscension
            this.declination = data.declination
            this.width = data.width ?? this.width
            this.height = data.height ?? this.height
            this.fov = data.fov ?? this.fov
            if (data.rotation === 0 || data.rotation) this.rotation = data.rotation

            ngZone.run(() => this.frame())
        })
    }

    async ngOnInit() {
        this.hipsSurveys = await this.api.hipsSurveys()

        this.width = parseInt(localStorage.getItem('FRAMING_WIDTH') || '1280')
        this.height = parseInt(localStorage.getItem('FRAMING_HEIGHT') || '720')
        this.fov = parseFloat(localStorage.getItem('FRAMING_FOV') || '1.0')
        this.rotation = parseFloat(localStorage.getItem('FRAMING_ROTATION') || '0.0')
        const id = localStorage.getItem('FRAMING_HIPS_SURVEY') || 'CDS/P/DSS2/color'
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

            localStorage.setItem('FRAMING_WIDTH', `${this.width}`)
            localStorage.setItem('FRAMING_HEIGHT', `${this.height}`)
            localStorage.setItem('FRAMING_FOV', `${this.fov}`)
            localStorage.setItem('FRAMING_ROTATION', `${this.rotation}`)
            localStorage.setItem('FRAMING_HIPS_SURVEY', this.hipsSurvey!.id)
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