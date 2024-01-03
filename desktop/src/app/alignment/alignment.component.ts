import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { DARVState, Hemisphere } from '../../shared/types/alignment.types'
import { Camera, CameraPreference, CameraStartCapture, cameraPreferenceKey } from '../../shared/types/camera.types'
import { GuideDirection, GuideOutput } from '../../shared/types/guider.types'
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
    darvDirection?: GuideDirection
    darvStatus: DARVState | 'IDLE' = 'IDLE'
    darvRemainingTime = 0
    darvProgress = 0

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private storage: LocalStorageService,
        electron: ElectronService,
        ngZone: NgZone,
    ) {
        app.title = 'Alignment'

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                    this.updateCamera()
                })
            }
        })

        electron.on('CAMERA.ATTACHED', event => {
            ngZone.run(() => {
                this.cameras.push(event.device)
            })
        })

        electron.on('CAMERA.DETACHED', event => {
            ngZone.run(() => {
                const index = this.cameras.findIndex(e => e.name === event.device.name)

                if (index >= 0) {
                    if (this.cameras[index] === this.camera) {
                        this.camera = undefined
                        this.cameraConnected = false
                    }

                    this.cameras.splice(index, 1)
                }
            })
        })

        electron.on('GUIDE_OUTPUT.ATTACHED', event => {
            ngZone.run(() => {
                this.guideOutputs.push(event.device)
            })
        })

        electron.on('GUIDE_OUTPUT.DETACHED', event => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.name === event.device.name)

                if (index >= 0) {
                    if (this.guideOutputs[index] === this.guideOutput) {
                        this.guideOutput = undefined
                        this.guideOutputConnected = false
                    }

                    this.guideOutputs.splice(index, 1)
                }
            })
        })

        electron.on('GUIDE_OUTPUT.UPDATED', event => {
            if (event.device.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, event.device)
                    this.updateGuideOutput()
                })
            }
        })

        electron.on('DARV_ALIGNMENT.ELAPSED', event => {
            if (event.camera.name === this.camera?.name &&
                event.guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    this.darvStatus = event.state
                    this.darvRemainingTime = event.remainingTime
                    this.darvProgress = event.progress
                    this.darvInProgress = event.remainingTime > 0

                    if (event.state === 'FORWARD' || event.state === 'BACKWARD') {
                        this.darvDirection = event.direction
                    } else {
                        this.darvDirection = undefined
                    }
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
        const capture = this.makeCameraStartCapture(this.camera!)
        await this.api.darvStart(this.camera!, this.guideOutput!, this.darvDrift * 1000000, this.darvInitialPause * 1000000, direction, reversed, capture)
    }

    darvAzimuth() {
        this.darvStart('EAST')
    }

    darvAltitude() {
        this.darvStart('NORTH') // TODO: NORTH não é usado?
    }

    darvStop() {
        this.api.darvStop(this.camera!, this.guideOutput!)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera!)
    }

    private updateCamera() {
        if (!this.camera) {
            return
        }

        this.cameraConnected = this.camera.connected
    }

    private updateGuideOutput() {
        if (!this.guideOutput) {
            return
        }

        this.guideOutputConnected = this.guideOutput.connected
    }

    private makeCameraStartCapture(camera: Camera): CameraStartCapture {
        const preference = this.storage.get<CameraPreference>(cameraPreferenceKey(camera), {})

        return {
            exposureTime: 0,
            exposureAmount: 0,
            exposureDelay: 0,
            frameType: 'LIGHT',
            autoSave: false,
            autoSubFolderMode: 'OFF',
            x: preference.x ?? camera.minX,
            y: preference.y ?? camera.minY,
            width: preference.width ?? camera.maxWidth,
            height: preference.height ?? camera.maxHeight,
            binX: preference.binX ?? 1,
            binY: preference.binY ?? 1,
            gain: preference.gain ?? 0,
            offset: preference.offset ?? 0,
            frameFormat: preference.frameFormat ?? (camera.frameFormats[0] || ''),
        }
    }
}