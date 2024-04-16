import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AlignmentMethod, AlignmentPreference, DARVStart, DARVState, Hemisphere, TPPAStart, TPPAState } from '../../shared/types/alignment.types'
import { Angle } from '../../shared/types/atlas.types'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureTimeUnit } from '../../shared/types/camera.types'
import { EMPTY_GUIDE_OUTPUT, GuideDirection, GuideOutput } from '../../shared/types/guider.types'
import { EMPTY_MOUNT, Mount } from '../../shared/types/mount.types'
import { DEFAULT_SOLVER_TYPES, EMPTY_PLATE_SOLVER_PREFERENCE } from '../../shared/types/settings.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-alignment',
    templateUrl: './alignment.component.html',
    styleUrls: ['./alignment.component.scss'],
})
export class AlignmentComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera = structuredClone(EMPTY_CAMERA)

    mounts: Mount[] = []
    mount = structuredClone(EMPTY_MOUNT)

    guideOutputs: GuideOutput[] = []
    guideOutput = structuredClone(EMPTY_GUIDE_OUTPUT)

    tab = 0

    running = false
    alignmentMethod?: AlignmentMethod
    status: DARVState | TPPAState = 'IDLE'
    elapsedTime = 0
    remainingTime = 0
    progress = 0
    private id = ''

    readonly tppaRequest: TPPAStart = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        plateSolver: structuredClone(EMPTY_PLATE_SOLVER_PREFERENCE),
        startFromCurrentPosition: true,
        eastDirection: true,
        compensateRefraction: true,
        stopTrackingWhenDone: true,
        stepDistance: 10,
    }

    readonly plateSolverTypes = Array.from(DEFAULT_SOLVER_TYPES)
    tppaAzimuthError: Angle = `00°00'00"`
    tppaAzimuthErrorDirection = ''
    tppaAltitudeError: Angle = `00°00'00"`
    tppaAltitudeErrorDirection = ''
    tppaTotalError: Angle = `00°00'00"`
    tppaRightAscension: Angle = '00h00m00s'
    tppaDeclination: Angle = `00°00'00"`

    readonly darvRequest: DARVStart = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        initialPause: 5,
        exposureTime: 30,
        direction: 'NORTH',
        reversed: false
    }

    readonly driftExposureUnit = ExposureTimeUnit.SECOND
    readonly darvHemispheres: Hemisphere[] = ['NORTHERN', 'SOUTHERN']
    darvHemisphere: Hemisphere = 'NORTHERN'
    darvDirection?: GuideDirection

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
            if (event.device.id === this.camera.id) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
                })
            }
        })

        electron.on('CAMERA.ATTACHED', event => {
            ngZone.run(() => {
                this.cameras.push(event.device)
                this.cameras.sort(deviceComparator)
            })
        })

        electron.on('CAMERA.DETACHED', event => {
            ngZone.run(() => {
                const index = this.cameras.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.cameras[index] === this.camera) {
                        Object.assign(this.camera, this.cameras[0] ?? EMPTY_CAMERA)
                    }

                    this.cameras.splice(index, 1)
                    this.cameras.sort(deviceComparator)
                }
            })
        })

        electron.on('MOUNT.UPDATED', event => {
            if (event.device.id === this.mount.id) {
                ngZone.run(() => {
                    Object.assign(this.mount, event.device)
                })
            }
        })

        electron.on('MOUNT.ATTACHED', event => {
            ngZone.run(() => {
                this.mounts.push(event.device)
                this.mounts.sort(deviceComparator)
            })
        })

        electron.on('MOUNT.DETACHED', event => {
            ngZone.run(() => {
                const index = this.mounts.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.mounts[index] === this.mount) {
                        Object.assign(this.mount, this.mounts[0] ?? EMPTY_MOUNT)
                    }

                    this.mounts.splice(index, 1)
                    this.mounts.sort(deviceComparator)
                }
            })
        })

        electron.on('GUIDE_OUTPUT.UPDATED', event => {
            if (event.device.id === this.guideOutput.id) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput, event.device)
                })
            }
        })

        electron.on('GUIDE_OUTPUT.ATTACHED', event => {
            ngZone.run(() => {
                this.guideOutputs.push(event.device)
                this.guideOutputs.sort(deviceComparator)
            })
        })

        electron.on('GUIDE_OUTPUT.DETACHED', event => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.guideOutputs[index] === this.guideOutput) {
                        Object.assign(this.guideOutput, this.guideOutputs[0] ?? EMPTY_GUIDE_OUTPUT)
                    }

                    this.guideOutputs.splice(index, 1)
                    this.guideOutputs.sort(deviceComparator)
                }
            })
        })

        electron.on('TPPA.ELAPSED', event => {
            if (event.id === this.id) {
                ngZone.run(() => {
                    if (this.status !== 'PAUSING' || event.state === 'PAUSED') {
                        this.status = event.state
                    }

                    this.running = event.state !== 'FINISHED'
                    this.elapsedTime = event.elapsedTime

                    if (event.state === 'COMPUTED') {
                        this.tppaAzimuthError = event.azimuthError
                        this.tppaAltitudeError = event.altitudeError
                        this.tppaAzimuthErrorDirection = event.azimuthErrorDirection
                        this.tppaAltitudeErrorDirection = event.altitudeErrorDirection
                        this.tppaTotalError = event.totalError
                    } else if (event.state === 'SOLVED' || event.state === 'SLEWING') {
                        this.tppaRightAscension = event.rightAscension
                        this.tppaDeclination = event.declination
                    }

                    if (!this.running) {
                        this.alignmentMethod = undefined
                    }
                })
            }
        })

        electron.on('DARV.ELAPSED', event => {
            if (event.id === this.id) {
                ngZone.run(() => {
                    this.status = event.state
                    this.remainingTime = event.remainingTime
                    this.progress = event.progress
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

        this.loadPreference()
    }

    async ngAfterViewInit() {
        this.cameras = (await this.api.cameras()).sort(deviceComparator)
        this.mounts = (await this.api.mounts()).sort(deviceComparator)
        this.guideOutputs = (await this.api.guideOutputs()).sort(deviceComparator)
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.darvStop()
        this.tppaStop()
    }

    async cameraChanged() {
        if (this.camera.id) {
            const camera = await this.api.camera(this.camera.id)
            Object.assign(this.camera, camera)
            this.loadPreference()
        }
    }

    async mountChanged() {
        if (this.mount.id) {
            const mount = await this.api.mount(this.mount.id)
            Object.assign(this.mount, mount)
        }
    }

    async guideOutputChanged() {
        if (this.guideOutput.id) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.id)
            Object.assign(this.guideOutput, guideOutput)
        }
    }

    mountConnect() {
        if (this.mount.id) {
            if (this.mount.connected) {
                this.api.mountDisconnect(this.mount)
            } else {
                this.api.mountConnect(this.mount)
            }
        }
    }

    guideOutputConnect() {
        if (this.guideOutput.id) {
            if (this.guideOutput.connected) {
                this.api.guideOutputDisconnect(this.guideOutput)
            } else {
                this.api.guideOutputConnect(this.guideOutput)
            }
        }
    }

    async showCameraDialog() {
        if (this.camera.id) {
            if (this.tab === 0) {
                if (await CameraComponent.showAsDialog(this.browserWindow, 'TPPA', this.camera, this.tppaRequest.capture)) {
                    this.savePreference()
                }
            } else if (this.tab === 1) {
                this.darvRequest.capture.exposureTime = this.darvRequest.exposureTime * 1000000
                this.darvRequest.capture.exposureDelay = this.darvRequest.initialPause

                if (await CameraComponent.showAsDialog(this.browserWindow, 'DARV', this.camera, this.darvRequest.capture)) {
                    this.savePreference()
                }
            }
        }
    }

    plateSolverChanged() {
        this.tppaRequest.plateSolver = this.preference.plateSolverPreference(this.tppaRequest.plateSolver.type).get()
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
        this.id = await this.api.darvStart(this.camera, this.guideOutput, this.darvRequest)
    }

    darvStop() {
        this.api.darvStop(this.id)
    }

    async tppaStart() {
        this.alignmentMethod = 'TPPA'
        await this.openCameraImage()
        this.id = await this.api.tppaStart(this.camera, this.mount, this.tppaRequest)
    }

    tppaPause() {
        this.status = 'PAUSING'
        this.api.tppaPause(this.id)
    }

    tppaUnpause() {
        this.api.tppaUnpause(this.id)
    }

    tppaStop() {
        this.api.tppaStop(this.id)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera)
    }

    private loadPreference() {
        const preference = this.preference.alignmentPreference.get()

        this.tppaRequest.startFromCurrentPosition = preference.tppaStartFromCurrentPosition
        this.tppaRequest.eastDirection = preference.tppaEastDirection
        this.tppaRequest.compensateRefraction = preference.tppaCompensateRefraction
        this.tppaRequest.stopTrackingWhenDone = preference.tppaStopTrackingWhenDone
        this.tppaRequest.stepDistance = preference.tppaStepDistance
        this.tppaRequest.plateSolver.type = preference.tppaPlateSolverType
        this.darvRequest.initialPause = preference.darvInitialPause
        this.darvRequest.exposureTime = preference.darvExposureTime
        this.darvHemisphere = preference.darvHemisphere

        if (this.camera.id) {
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
            tppaCompensateRefraction: this.tppaRequest.compensateRefraction,
            tppaStopTrackingWhenDone: this.tppaRequest.stopTrackingWhenDone,
            tppaStepDistance: this.tppaRequest.stepDistance,
            tppaPlateSolverType: this.tppaRequest.plateSolver.type,
            darvInitialPause: this.darvRequest.initialPause,
            darvExposureTime: this.darvRequest.exposureTime,
            darvHemisphere: this.darvHemisphere,
        }

        this.preference.alignmentPreference.set(preference)
    }
}