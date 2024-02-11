import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AlignmentMethod, AlignmentPreference, DARVStart, DARVState, Hemisphere, TPPAStart, TPPAState } from '../../shared/types/alignment.types'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureTimeUnit } from '../../shared/types/camera.types'
import { EMPTY_GUIDE_OUTPUT, GuideDirection, GuideOutput } from '../../shared/types/guider.types'
import { EMPTY_MOUNT, Mount } from '../../shared/types/mount.types'
import { DEFAULT_SOLVER_TYPES, EMPTY_PLATE_SOLVER_OPTIONS, PlateSolverType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-alignment',
    templateUrl: './alignment.component.html',
    styleUrls: ['./alignment.component.scss'],
})
export class AlignmentComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera = Object.assign({}, EMPTY_CAMERA)

    mounts: Mount[] = []
    mount = Object.assign({}, EMPTY_MOUNT)

    guideOutputs: GuideOutput[] = []
    guideOutput = Object.assign({}, EMPTY_GUIDE_OUTPUT)

    tab = 0

    running = false
    alignmentMethod?: AlignmentMethod
    status: DARVState | TPPAState = 'IDLE'

    readonly tppaRequest: TPPAStart = {
        capture: Object.assign({}, EMPTY_CAMERA_START_CAPTURE),
        plateSolver: Object.assign({}, EMPTY_PLATE_SOLVER_OPTIONS),
        startFromCurrentPosition: true,
        eastDirection: true,
        refractionAdjustment: true,
        stopTrackingWhenDone: true,
        stepDistance: 10,
    }

    readonly plateSolverTypes: PlateSolverType[] = Object.assign([], DEFAULT_SOLVER_TYPES)

    readonly darvRequest: DARVStart = {
        capture: Object.assign({}, EMPTY_CAMERA_START_CAPTURE),
        initialPause: 5,
        exposureTime: 30,
        direction: 'NORTH',
        reversed: false
    }

    readonly driftExposureUnit = ExposureTimeUnit.SECOND
    readonly darvHemispheres: Hemisphere[] = ['NORTHERN', 'SOUTHERN']
    darvHemisphere: Hemisphere = 'NORTHERN'
    darvDirection?: GuideDirection
    darvRemainingTime = 0
    darvProgress = 0

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private preference: PreferenceService,
        electron: ElectronService,
        ngZone: NgZone,
    ) {
        app.title = 'Alignment'

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.name === this.camera.name) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
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
                        Object.assign(this.camera, EMPTY_CAMERA)
                    }

                    this.cameras.splice(index, 1)
                }
            })
        })

        electron.on('MOUNT.UPDATED', event => {
            if (event.device.name === this.mount.name) {
                ngZone.run(() => {
                    Object.assign(this.mount, event.device)
                })
            }
        })

        electron.on('MOUNT.ATTACHED', event => {
            ngZone.run(() => {
                this.mounts.push(event.device)
            })
        })

        electron.on('MOUNT.DETACHED', event => {
            ngZone.run(() => {
                const index = this.mounts.findIndex(e => e.name === event.device.name)

                if (index >= 0) {
                    if (this.mounts[index] === this.mount) {
                        Object.assign(this.mount, EMPTY_MOUNT)
                    }

                    this.mounts.splice(index, 1)
                }
            })
        })

        electron.on('GUIDE_OUTPUT.UPDATED', event => {
            if (event.device.name === this.guideOutput.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput, event.device)
                })
            }
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
                        Object.assign(this.guideOutput, EMPTY_GUIDE_OUTPUT)
                    }

                    this.guideOutputs.splice(index, 1)
                }
            })
        })

        electron.on('TPPA_ALIGNMENT.ELAPSED', event => {
            if (event.camera.name === this.camera.name &&
                (!event.mount || event.mount.name === this.mount.name)) {
                ngZone.run(() => {
                    this.status = event.state
                    this.running = event.state !== 'FINISHED'

                    if (!this.running) {
                        this.alignmentMethod = undefined
                    }
                })
            }
        })

        electron.on('DARV_ALIGNMENT.ELAPSED', event => {
            if (event.camera.name === this.camera.name &&
                event.guideOutput.name === this.guideOutput.name) {
                ngZone.run(() => {
                    this.status = event.state
                    this.darvRemainingTime = event.remainingTime
                    this.darvProgress = event.progress
                    this.running = event.remainingTime > 0

                    if (event.state === 'FORWARD' || event.state === 'BACKWARD') {
                        this.darvDirection = event.direction
                    } else {
                        this.darvDirection = undefined
                    }

                    if (!this.running) {
                        this.alignmentMethod = undefined
                    }
                })
            }
        })
    }

    async ngAfterViewInit() {
        this.cameras = await this.api.cameras()
        this.mounts = await this.api.mounts()
        this.guideOutputs = await this.api.guideOutputs()
        this.loadPreference()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.darvStop()
        this.tppaStop()
    }

    async cameraChanged() {
        if (this.camera.name) {
            const camera = await this.api.camera(this.camera.name)
            Object.assign(this.camera, camera)
            this.loadPreference()
        }
    }

    async mountChanged() {
        if (this.mount.name) {
            const mount = await this.api.mount(this.mount.name)
            Object.assign(this.mount, mount)
        }
    }

    async guideOutputChanged() {
        if (this.guideOutput.name) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.name)
            Object.assign(this.guideOutput, guideOutput)
        }
    }

    mountConnect() {
        if (this.mount.name) {
            if (this.mount.connected) {
                this.api.mountDisconnect(this.mount)
            } else {
                this.api.mountConnect(this.mount)
            }
        }
    }

    guideOutputConnect() {
        if (this.guideOutput.name) {
            if (this.guideOutput.connected) {
                this.api.guideOutputDisconnect(this.guideOutput)
            } else {
                this.api.guideOutputConnect(this.guideOutput)
            }
        }
    }

    async showCameraDialog() {
        if (this.camera.name) {
            if (this.tab === 0 && await CameraComponent.showAsDialog(this.browserWindow, 'TPPA', this.camera, this.tppaRequest.capture)) {
                this.savePreference()
            } else if (this.tab === 1 && await CameraComponent.showAsDialog(this.browserWindow, 'DARV', this.camera, this.darvRequest.capture)) {
                this.savePreference()
                this.darvRequest.exposureTime = this.darvRequest.capture.exposureTime / 1000000
                this.darvRequest.initialPause = this.darvRequest.capture.exposureDelay
            }
        }
    }

    plateSolverChanged() {
        this.tppaRequest.plateSolver = this.preference.plateSolverOptions(this.tppaRequest.plateSolver.type).get()
        this.savePreference()
    }

    initialPauseChanged() {
        this.darvRequest.capture.exposureDelay = this.darvRequest.initialPause
        this.savePreference()
    }

    driftForChanged() {
        this.darvRequest.capture.exposureTime = this.darvRequest.exposureTime * 1000000
        this.savePreference()
    }

    async darvStart(direction: GuideDirection = 'EAST') {
        this.alignmentMethod = 'DARV'
        this.darvRequest.direction = direction
        this.darvRequest.reversed = this.darvHemisphere === 'SOUTHERN'
        this.darvRequest.capture.exposureTime = this.darvRequest.exposureTime * 1000000
        this.darvRequest.capture.exposureDelay = this.darvRequest.initialPause
        await this.openCameraImage()
        await this.api.darvStart(this.camera, this.guideOutput, this.darvRequest)
    }

    darvStop() {
        this.api.darvStop(this.camera, this.guideOutput)
    }

    async tppaStart() {
        this.alignmentMethod = 'TPPA'
        await this.openCameraImage()
        await this.api.tppaStart(this.camera, this.mount, this.tppaRequest)
    }

    tppaStop() {
        this.api.tppaStop(this.camera, this.mount)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera)
    }

    private loadPreference() {
        const preference = this.preference.alignmentPreference().get()

        this.tppaRequest.startFromCurrentPosition = preference.tppaStartFromCurrentPosition
        this.tppaRequest.eastDirection = preference.tppaEastDirection
        this.tppaRequest.refractionAdjustment = preference.tppaRefractionAdjustment
        this.tppaRequest.stopTrackingWhenDone = preference.tppaStopTrackingWhenDone
        this.tppaRequest.stepDistance = preference.tppaStepDistance
        this.tppaRequest.plateSolver.type = preference.tppaPlateSolverType
        this.darvRequest.initialPause = preference.darvInitialPause
        this.darvRequest.exposureTime = preference.darvExposureTime
        this.darvHemisphere = preference.darvHemisphere

        if (this.camera.name) {
            Object.assign(this.tppaRequest.capture, this.preference.cameraStartCaptureForTPPA(this.camera).get(this.tppaRequest.capture))
            Object.assign(this.darvRequest.capture, this.preference.cameraStartCaptureForDARV(this.camera).get(this.darvRequest.capture))
        }

        this.plateSolverChanged()
    }

    savePreference() {
        if (this.tab === 0) {
            this.preference.cameraStartCaptureForTPPA(this.camera).set(this.tppaRequest.capture)
        } else if (this.tab === 1) {
            this.preference.cameraStartCaptureForDARV(this.camera).set(this.darvRequest.capture)
        }

        const preference: AlignmentPreference = {
            tppaStartFromCurrentPosition: this.tppaRequest.startFromCurrentPosition,
            tppaEastDirection: this.tppaRequest.eastDirection,
            tppaRefractionAdjustment: this.tppaRequest.refractionAdjustment,
            tppaStopTrackingWhenDone: this.tppaRequest.stopTrackingWhenDone,
            tppaStepDistance: this.tppaRequest.stepDistance,
            tppaPlateSolverType: this.tppaRequest.plateSolver.type,
            darvInitialPause: this.darvRequest.initialPause,
            darvExposureTime: this.darvRequest.exposureTime,
            darvHemisphere: this.darvHemisphere,
        }

        this.preference.alignmentPreference().set(preference)
    }
}