import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { GuideDirection, GuideOutput, GuideStar, GuideState, Guider } from '../../shared/types'

@Component({
    selector: 'app-guider',
    templateUrl: './guider.component.html',
    styleUrls: ['./guider.component.scss'],
})
export class GuiderComponent implements AfterViewInit, OnDestroy {

    guideOutputs: GuideOutput[] = []
    guideOutput?: GuideOutput
    guideOutputConnected = false
    pulseGuiding = false

    guideNorthDuration = 1000
    guideSouthDuration = 1000
    guideWestDuration = 1000
    guideEastDuration = 1000

    phdConnected = false
    phdHost = 'localhost'
    phdPort = 4400
    phdState: GuideState = 'STOPPED'
    phdGuideStar?: GuideStar
    phdMessage = ''

    ditherPixels = 5
    ditherRAOnly = false
    settlePixels = 1.5
    settleTime = 60
    settleTimeout = 90
    autoSelectStar = true

    get stopped() {
        return this.phdState === 'STOPPED'
    }

    get looping() {
        return this.phdState === 'LOOPING'
    }

    get guiding() {
        return this.phdState === 'GUIDING'
    }

    constructor(
        title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Guider')

        api.startListening('GUIDING')

        electron.on('GUIDE_OUTPUT_UPDATED', (_, event: GuideOutput) => {
            if (event.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, event)
                    this.update()
                })
            }
        })

        electron.on('GUIDE_OUTPUT_ATTACHED', (_, event: GuideOutput) => {
            ngZone.run(() => {
                this.guideOutputs.push(event)
            })
        })

        electron.on('GUIDE_OUTPUT_DETACHED', (_, event: GuideOutput) => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.name === event.name)
                if (index) this.guideOutputs.splice(index, 1)
            })
        })

        electron.on('GUIDER_CONNECTED', () => {
            ngZone.run(() => {
                this.phdConnected = true
            })
        })

        electron.on('GUIDER_DISCONNECTED', () => {
            ngZone.run(() => {
                this.phdConnected = true
            })
        })

        electron.on('GUIDER_UPDATED', (_, event: Guider) => {
            ngZone.run(() => {
                this.phdState = event.state
            })
        })

        electron.on('GUIDER_STEPPED', (_, event: GuideStar) => {
            ngZone.run(() => {
                this.phdGuideStar = event
            })
        })

        electron.on('GUIDER_MESSAGE_RECEIVED', (_, event: { message: string }) => {
            ngZone.run(() => {
                this.phdMessage = event.message
            })
        })
    }

    async ngAfterViewInit() {
        this.guideOutputs = await this.api.guideOutputs()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.stopListening('GUIDING')
    }

    async guideOutputChanged() {
        if (this.guideOutput) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.name)
            Object.assign(this.guideOutput, guideOutput)

            this.update()
        }

        this.electron.send('GUIDE_OUTPUT_CHANGED', this.guideOutput)
    }

    connectGuideOutput() {
        if (this.guideOutputConnected) {
            this.api.guideOutputDisconnect(this.guideOutput!)
        } else {
            this.api.guideOutputConnect(this.guideOutput!)
        }
    }

    guidePulseStart(...directions: GuideDirection[]) {
        for (const direction of directions) {
            switch (direction) {
                case 'NORTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideNorthDuration)
                    break
                case 'SOUTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideSouthDuration)
                    break
                case 'WEST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideWestDuration)
                    break
                case 'EAST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideEastDuration)
                    break
            }
        }
    }

    guidePulseStop() {
        this.api.guideOutputPulse(this.guideOutput!, 'NORTH', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'SOUTH', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'WEST', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'EAST', 0)
    }

    connectPHD2() {
        if (this.phdConnected) {
            this.api.guidingDisconnect()
        } else {
            this.api.guidingConnect(this.phdHost, this.phdPort)
        }
    }

    async guidingStart(event: MouseEvent) {
        await this.api.guidingLoop(this.autoSelectStar)
        await this.api.guidingStart(event.shiftKey)
    }

    guidingStop() {
        this.api.guidingStop()
    }

    private update() {
        if (this.guideOutput) {
            this.guideOutputConnected = this.guideOutput.connected
            this.pulseGuiding = this.guideOutput.pulseGuiding
        }
    }
}