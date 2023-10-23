import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, CameraCaptureEvent, DARVPolarAlignmentEvent, DARVPolarAlignmentGuidePulseElapsed, DARVPolarAlignmentInitialPauseElapsed, GuideDirection, GuideOutput, Hemisphere } from '../../shared/types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-alignment',
    templateUrl: './alignment.component.html',
    styleUrls: ['./alignment.component.scss'],
})
export class AlignmentComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera?: Camera
    cameraConnected = false

    guideOutputs: GuideOutput[] = []
    guideOutput?: GuideOutput
    guideOutputConnected = false

    darvInitialPause = 5
    darvDrift = 30
    darvInProgress = false
    readonly darvHemispheres: Hemisphere[] = ['NORTHERN', 'SOUTHERN']
    darvHemisphere: Hemisphere = 'NORTHERN'
    darvCaptureEvent?: CameraCaptureEvent
    darvDirection?: GuideDirection
    darvStatus = 'idle'

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        app.title = 'Alignment'

        electron.on('CAMERA_UPDATED', (_, event: Camera) => {
            if (event.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event)
                    this.updateCamera()
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
                if (index >= 0) this.guideOutputs.splice(index, 1)
            })
        })

        electron.on('GUIDE_OUTPUT_UPDATED', (_, event: GuideOutput) => {
            if (event.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, event)
                    this.updateGuideOutput()
                })
            }
        })

        electron.on('DARV_POLAR_ALIGNMENT_STARTED', (_, event: DARVPolarAlignmentEvent) => {
            if (event.camera.name === this.camera?.name &&
                event.guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    this.darvInProgress = true
                })
            }
        })

        electron.on('DARV_POLAR_ALIGNMENT_FINISHED', (_, event: DARVPolarAlignmentEvent) => {
            if (event.camera.name === this.camera?.name &&
                event.guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    this.darvInProgress = false
                    this.darvStatus = 'idle'
                    this.darvDirection = undefined
                })
            }
        })

        electron.on('DARV_POLAR_ALIGNMENT_INITIAL_PAUSE_ELAPSED', (_, event: DARVPolarAlignmentInitialPauseElapsed) => {
            if (event.camera.name === this.camera?.name &&
                event.guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    this.darvStatus = 'initial pause'
                })
            }
        })

        electron.on('DARV_POLAR_ALIGNMENT_GUIDE_PULSE_ELAPSED', (_, event: DARVPolarAlignmentGuidePulseElapsed) => {
            if (event.camera.name === this.camera?.name &&
                event.guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    this.darvDirection = event.direction
                    this.darvStatus = event.forward ? 'forwarding' : 'backwarding'
                })
            }
        })

        electron.on('CAMERA_EXPOSURE_UPDATED', (_, event: CameraCaptureEvent) => {
            if (event.camera.name === this.camera?.name) {
                ngZone.run(() => {
                    this.darvCaptureEvent = event
                })
            }
        })
    }

    async ngAfterViewInit() {
        this.cameras = await this.api.cameras()
        this.guideOutputs = await this.api.guideOutputs()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.darvStop()
    }

    async cameraChanged() {
        if (this.camera) {
            const camera = await this.api.camera(this.camera.name)
            Object.assign(this.camera, camera)

            this.updateCamera()
        }
    }

    async guideOutputChanged() {
        if (this.guideOutput) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.name)
            Object.assign(this.guideOutput, guideOutput)

            this.updateGuideOutput()
        }
    }

    cameraConnect() {
        if (this.cameraConnected) {
            this.api.cameraDisconnect(this.camera!)
        } else {
            this.api.cameraConnect(this.camera!)
        }
    }

    guideOutputConnect() {
        if (this.guideOutputConnected) {
            this.api.guideOutputDisconnect(this.guideOutput!)
        } else {
            this.api.guideOutputConnect(this.guideOutput!)
        }
    }

    private async darvStart(direction: GuideDirection) {
        // TODO: Horizonte leste e oeste tem um impacto no "reversed"?
        const reversed = this.darvHemisphere === 'SOUTHERN'
        await this.openCameraImage()
        await this.api.darvStart(this.camera!, this.guideOutput!, this.darvDrift, this.darvInitialPause, direction, reversed)
    }

    darvAzimuth() {
        this.darvStart('EAST')
    }

    darvAltitude() {
        this.darvStart('EAST') // TODO: NORTH não é usado?
    }

    darvStop() {
        this.api.darvStop(this.camera!, this.guideOutput!)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera!)
    }

    private async updateCamera() {
        if (!this.camera) {
            return
        }

        this.cameraConnected = this.camera.connected
    }

    private async updateGuideOutput() {
        if (!this.guideOutput) {
            return
        }

        this.guideOutputConnected = this.guideOutput.connected
    }
}