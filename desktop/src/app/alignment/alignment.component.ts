import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AlignmentMethod, AlignmentPreference, DARVStart, DARVState, Hemisphere, TPPAStart, TPPAState } from '../../shared/types/alignment.types'
import { Angle } from '../../shared/types/atlas.types'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, ExposureTimeUnit, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { EMPTY_GUIDE_OUTPUT, GuideDirection, GuideOutput } from '../../shared/types/guider.types'
import { EMPTY_MOUNT, Mount } from '../../shared/types/mount.types'
import { EMPTY_PLATE_SOLVER_OPTIONS } from '../../shared/types/settings.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-alignment',
    templateUrl: './alignment.component.html',
    styleUrls: ['./alignment.component.scss'],
})
export class AlignmentComponent implements AfterViewInit, OnDestroy, Pingable {

    cameras: Camera[] = []
    camera = structuredClone(EMPTY_CAMERA)

    mounts: Mount[] = []
    mount = structuredClone(EMPTY_MOUNT)

    guideOutputs: GuideOutput[] = []
    guideOutput = structuredClone(EMPTY_GUIDE_OUTPUT)

    tab = 0

    running = false
    pausingOrPaused = false
    alignmentMethod?: AlignmentMethod
    status: DARVState | TPPAState = 'IDLE'

    readonly tppaRequest: TPPAStart = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        plateSolver: structuredClone(EMPTY_PLATE_SOLVER_OPTIONS),
        startFromCurrentPosition: true,
        stepDirection: 'EAST',
        compensateRefraction: true,
        stopTrackingWhenDone: true,
        stepDuration: 5,
    }

    tppaFailed = false
    tppaRightAscension: Angle = `00h00m00s`
    tppaDeclination: Angle = `00°00'00"`
    tppaAzimuthError: Angle = `00°00'00"`
    tppaAzimuthErrorDirection = ''
    tppaAltitudeError: Angle = `00°00'00"`
    tppaAltitudeErrorDirection = ''
    tppaTotalError: Angle = `00°00'00"`

    readonly darvRequest: DARVStart = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        initialPause: 5,
        exposureTime: 30,
        direction: 'NORTH',
        reversed: false
    }

    readonly driftExposureUnit = ExposureTimeUnit.SECOND
    darvHemisphere: Hemisphere = 'NORTHERN'
    darvDirection?: GuideDirection

    @ViewChild('cameraExposure')
    private readonly cameraExposure!: CameraExposureComponent

    private autoResizeTimeout?: any

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private preference: PreferenceService,
        private pinger: Pinger,
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
                }
            })
        })

        electron.on('TPPA.ELAPSED', event => {
            if (event.camera.id === this.camera?.id) {
                ngZone.run(() => {
                    this.status = event.state
                    this.running = event.state !== 'FINISHED'
                    this.pausingOrPaused = event.state === 'PAUSING' || event.state === 'PAUSED'

                    if (event.state === 'COMPUTED') {
                        this.tppaFailed = false
                        this.tppaRightAscension = event.rightAscension
                        this.tppaDeclination = event.declination
                        this.tppaAzimuthError = event.azimuthError
                        this.tppaAltitudeError = event.altitudeError
                        this.tppaAzimuthErrorDirection = event.azimuthErrorDirection
                        this.tppaAltitudeErrorDirection = event.altitudeErrorDirection
                        this.tppaTotalError = event.totalError
                        clearTimeout(this.autoResizeTimeout)
                        this.autoResizeTimeout = electron.autoResizeWindow()
                    } else if (event.state === 'FINISHED') {
                        this.cameraExposure.reset()
                        electron.autoResizeWindow()
                    } else if (event.state === 'SOLVED' || event.state === 'SLEWED') {
                        this.tppaFailed = false
                        this.tppaRightAscension = event.rightAscension
                        this.tppaDeclination = event.declination
                    } else if (event.state === 'FAILED') {
                        this.tppaFailed = true
                    }

                    if (event.capture && event.capture.state !== 'CAPTURE_FINISHED') {
                        this.cameraExposure.handleCameraCaptureEvent(event.capture, true)
                    }
                })
            }
        })

        electron.on('DARV.ELAPSED', event => {
            if (event.camera.id === this.camera?.id) {
                ngZone.run(() => {
                    this.status = event.state
                    this.running = this.cameraExposure.handleCameraCaptureEvent(event.capture)

                    if (event.state === 'FORWARD' || event.state === 'BACKWARD') {
                        this.darvDirection = event.direction
                    } else {
                        this.darvDirection = undefined
                    }
                })
            }
        })

        this.loadPreference()

        pinger.register(this, 30000)
    }

    async ngAfterViewInit() {
        this.cameras = (await this.api.cameras()).sort(deviceComparator)
        this.mounts = (await this.api.mounts()).sort(deviceComparator)
        this.guideOutputs = (await this.api.guideOutputs()).sort(deviceComparator)
    }

    @HostListener('window:unload')
    async ngOnDestroy() {
        this.pinger.unregister(this)

        this.darvStop()
        this.tppaStop()
    }

    ping() {
        if (this.camera.id) this.api.cameraListen(this.camera)
        if (this.mount.id) this.api.mountListen(this.mount)
        if (this.guideOutput.id) this.api.guideOutputListen(this.guideOutput)
    }

    async cameraChanged() {
        if (this.camera.id) {
            this.ping()

            const camera = await this.api.camera(this.camera.id)
            Object.assign(this.camera, camera)
        }
    }

    async mountChanged() {
        if (this.mount.id) {
            this.ping()

            const mount = await this.api.mount(this.mount.id)
            Object.assign(this.mount, mount)
            this.tppaRequest.stepSpeed = mount.slewRate?.name
        }
    }

    async guideOutputChanged() {
        if (this.guideOutput.id) {
            this.ping()

            const guideOutput = await this.api.guideOutput(this.guideOutput.id)
            Object.assign(this.guideOutput, guideOutput)
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
        return this.api.darvStop(this.camera)
    }

    async tppaStart() {
        this.alignmentMethod = 'TPPA'
        await this.openCameraImage()
        await this.api.tppaStart(this.camera, this.mount, this.tppaRequest)
    }

    tppaPause() {
        return this.api.tppaPause(this.camera)
    }

    tppaUnpause() {
        return this.api.tppaUnpause(this.camera)
    }

    tppaStop() {
        return this.api.tppaStop(this.camera)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera, 'ALIGNMENT')
    }

    private loadPreference() {
        const preference = this.preference.alignmentPreference.get()

        this.tppaRequest.startFromCurrentPosition = preference.tppaStartFromCurrentPosition
        this.tppaRequest.stepDirection = preference.tppaStepDirection
        this.tppaRequest.compensateRefraction = preference.tppaCompensateRefraction
        this.tppaRequest.stopTrackingWhenDone = preference.tppaStopTrackingWhenDone
        this.tppaRequest.stepDuration = preference.tppaStepDuration
        this.tppaRequest.plateSolver.type = preference.tppaPlateSolverType
        this.darvRequest.initialPause = preference.darvInitialPause
        this.darvRequest.exposureTime = preference.darvExposureTime
        this.darvHemisphere = preference.darvHemisphere

        if (this.camera.id) {
            const cameraPreference = this.preference.cameraPreference(this.camera).get()
            Object.assign(this.tppaRequest.capture, this.preference.cameraStartCaptureForTPPA(this.camera).get(cameraPreference))
            Object.assign(this.darvRequest.capture, this.preference.cameraStartCaptureForDARV(this.camera).get(cameraPreference))

            if (this.camera.connected) {
                updateCameraStartCaptureFromCamera(this.tppaRequest.capture, this.camera)
                updateCameraStartCaptureFromCamera(this.darvRequest.capture, this.camera)
            }
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
            tppaStepDirection: this.tppaRequest.stepDirection,
            tppaCompensateRefraction: this.tppaRequest.compensateRefraction,
            tppaStopTrackingWhenDone: this.tppaRequest.stopTrackingWhenDone,
            tppaStepDuration: this.tppaRequest.stepDuration,
            tppaPlateSolverType: this.tppaRequest.plateSolver.type,
            darvInitialPause: this.darvRequest.initialPause,
            darvExposureTime: this.darvRequest.exposureTime,
            darvHemisphere: this.darvHemisphere,
        }

        this.preference.alignmentPreference.set(preference)
    }
}