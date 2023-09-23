import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MessageService } from 'primeng/api'
import hipsSurveys from '../../assets/data/hipsSurveys.json'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Angle, HipsSurvey } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface FramingParams {
    rightAscension: Angle
    declination: Angle
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
export class FramingComponent implements AfterViewInit, OnDestroy {

    rightAscension: Angle = '00h00m00s'
    declination: Angle = `+000°00'00"`
    width = 1280
    height = 720
    fov = 1.0
    rotation = 0.0
    readonly hipsSurveys: HipsSurvey[] = hipsSurveys
    hipsSurvey: HipsSurvey = hipsSurveys[0]

    loading = false

    private framePath?: string
    private frameId = ''

    constructor(
        app: AppComponent,
        private route: ActivatedRoute,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private message: MessageService,
        ngZone: NgZone,
    ) {
        app.title = 'Framing'

        this.loadPreference()

        electron.on('PARAMS_CHANGED', (_, data: FramingParams) => {
            ngZone.run(() => this.frameFromParams(data))
        })
    }

    ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(decodeURIComponent(e.params)) as FramingParams
            this.frameFromParams(params)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.closeFrameImage()
        this.electron.sendSync('CLOSE_WINDOW', this.frameId)
    }

    private frameFromParams(params: FramingParams) {
        this.rightAscension = params.rightAscension ?? this.rightAscension
        this.declination = params.declination ?? this.declination
        this.width = params.width ?? this.width
        this.height = params.height ?? this.height
        this.fov = params.fov ?? this.fov
        if (params.rotation === 0 || params.rotation) this.rotation = params.rotation

        if (params.rightAscension && params.declination) {
            this.frame()
        }
    }

    async frame() {
        if (!this.hipsSurvey) return

        await this.closeFrameImage()

        this.loading = true

        try {
            const path = await this.api.frame(this.rightAscension, this.declination, this.width, this.height, this.fov, this.rotation, this.hipsSurvey!)
            const title = `Framing ・ ${this.rightAscension} ・ ${this.declination}`

            this.framePath = path
            this.frameId = await this.browserWindow.openImage(path, 'framing', 'FRAMING', title)

            this.savePreference()
        } catch (e: any) {
            console.error(e)

            this.message.add({ severity: 'error', detail: e.message || 'Failed to retrieve the image' })
        } finally {
            this.loading = false
        }
    }

    private loadPreference() {
        this.rightAscension = this.preference.get('framing.rightAscension', '00h00m00s')
        this.declination = this.preference.get('framing.declination', `+000°00'00"`)
        this.width = this.preference.get('framing.width', 1280)
        this.height = this.preference.get('framing.height', 720)
        this.fov = this.preference.get('framing.fov', 1)
        this.rotation = this.preference.get('framing.rotation', 0)
        this.hipsSurvey = this.preference.get('framing.hipsSurvey', this.hipsSurvey)
    }

    private savePreference() {
        this.preference.set('framing.rightAscension', this.rightAscension)
        this.preference.set('framing.declination', this.declination)
        this.preference.set('framing.width', this.width)
        this.preference.set('framing.height', this.height)
        this.preference.set('framing.fov', this.fov)
        this.preference.set('framing.rotation', this.rotation)
        this.preference.set('framing.hipsSurvey', this.hipsSurvey)
    }

    private async closeFrameImage() {
        if (this.framePath) {
            await this.api.closeImage(this.framePath)
        }
    }
}