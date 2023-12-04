import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MessageService } from 'primeng/api'
import hipsSurveys from '../../assets/data/hipsSurveys.json'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Angle, HipsSurvey } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface FramingPreference {
    rightAscension?: Angle
    declination?: Angle
    width?: number
    height?: number
    fov?: number
    rotation?: number
    hipsSurvey?: HipsSurvey
}

export interface FramingData {
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
        private storage: LocalStorageService,
        private message: MessageService,
        ngZone: NgZone,
    ) {
        app.title = 'Framing'

        electron.on('DATA_CHANGED', (event: FramingData) => {
            ngZone.run(() => this.frameFromData(event))
        })
    }

    ngAfterViewInit() {
        this.loadPreference()

        this.route.queryParams.subscribe(e => {
            const data = JSON.parse(decodeURIComponent(e.data)) as FramingData
            this.frameFromData(data)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.closeFrameImage()
        this.electron.send('CLOSE_WINDOW', this.frameId)
    }

    private frameFromData(data: FramingData) {
        this.rightAscension = data.rightAscension ?? this.rightAscension
        this.declination = data.declination ?? this.declination
        this.width = data.width ?? this.width
        this.height = data.height ?? this.height
        this.fov = data.fov ?? this.fov
        if (data.rotation === 0 || data.rotation) this.rotation = data.rotation

        if (data.rightAscension && data.declination) {
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
            this.frameId = await this.browserWindow.openImage({ id: 'framing', source: 'FRAMING', path, title })

            this.savePreference()
        } catch (e: any) {
            console.error(e)

            this.message.add({ severity: 'error', detail: e.message || 'Failed to retrieve the image' })
        } finally {
            this.loading = false
        }
    }

    private loadPreference() {
        const preference = this.storage.get<FramingPreference>('framing', {})

        this.rightAscension = preference.rightAscension ?? '00h00m00s'
        this.declination = preference.declination ?? `+00°00'00"`
        this.width = preference.width ?? 1280
        this.height = preference.height ?? 720
        this.fov = preference.fov ?? 1
        this.rotation = preference.rotation ?? 0
        this.hipsSurvey ??= preference.hipsSurvey ?? this.hipsSurvey
    }

    private savePreference() {
        const preference: FramingPreference = {
            rightAscension: this.rightAscension,
            declination: this.declination,
            width: this.width,
            height: this.height,
            fov: this.fov,
            rotation: this.rotation,
            hipsSurvey: this.hipsSurvey,
        }

        this.storage.set('framing', preference)
    }

    private async closeFrameImage() {
        if (this.framePath) {
            await this.api.closeImage(this.framePath)
        }
    }
}