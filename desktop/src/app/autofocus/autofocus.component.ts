import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AutoFocusPreference, AutoFocusRequest } from '../../shared/types/autofocus.type'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { EMPTY_FOCUSER, Focuser } from '../../shared/types/focuser.types'
import { EMPTY_STAR_DETECTION_OPTIONS } from '../../shared/types/settings.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-autofocus',
    templateUrl: './autofocus.component.html',
    styleUrls: ['./autofocus.component.scss'],
})
export class AutoFocusComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera = structuredClone(EMPTY_CAMERA)

    focusers: Focuser[] = []
    focuser = structuredClone(EMPTY_FOCUSER)

    running = false

    readonly request: AutoFocusRequest = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        fittingMode: 'HYPERBOLIC',
        rSquaredThreshold: 0.7,
        backlashCompensation: {
            mode: 'NONE',
            backlashIn: 0,
            backlashOut: 0
        },
        initialOffsetSteps: 4,
        stepSize: 100,
        totalNumberOfAttempts: 1,
        starDetector: structuredClone(EMPTY_STAR_DETECTION_OPTIONS),
    }

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private preference: PreferenceService,
        electron: ElectronService,
        ngZone: NgZone,
    ) {
        app.title = 'Auto Focus'

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

        electron.on('FOCUSER.UPDATED', event => {
            if (event.device.id === this.focuser.id) {
                ngZone.run(() => {
                    Object.assign(this.focuser, event.device)
                })
            }
        })

        electron.on('FOCUSER.ATTACHED', event => {
            ngZone.run(() => {
                this.focusers.push(event.device)
                this.focusers.sort(deviceComparator)
            })
        })

        electron.on('FOCUSER.DETACHED', event => {
            ngZone.run(() => {
                const index = this.focusers.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.focusers[index] === this.focuser) {
                        Object.assign(this.focuser, this.focusers[0] ?? EMPTY_FOCUSER)
                    }

                    this.focusers.splice(index, 1)
                }
            })
        })

        this.loadPreference()
    }

    async ngAfterViewInit() {
        this.cameras = (await this.api.cameras()).sort(deviceComparator)
        this.focusers = (await this.api.focusers()).sort(deviceComparator)
    }

    @HostListener('window:unload')
    async ngOnDestroy() {
        await this.stop()
    }

    async cameraChanged() {
        if (this.camera.id) {
            const camera = await this.api.camera(this.camera.id)
            Object.assign(this.camera, camera)
        }
    }

    async focuserChanged() {
        if (this.focuser.id) {
            const focuser = await this.api.focuser(this.focuser.id)
            Object.assign(this.focuser, focuser)
        }
    }

    async showCameraDialog() {
        if (this.camera.id) {
            if (await CameraComponent.showAsDialog(this.browserWindow, 'AUTO_FOCUS', this.camera, this.request.capture)) {
                this.savePreference()
            }
        }
    }

    async start() {
        await this.openCameraImage()
        this.request.starDetector = this.preference.starDetectionOptions('ASTAP').get()
        return this.api.autoFocusStart(this.camera, this.focuser, this.request)
    }

    stop() {
        return this.api.autoFocusStop(this.camera)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera, 'ALIGNMENT')
    }

    private loadPreference() {
        const preference = this.preference.autoFocusPreference.get()

        this.request.fittingMode = preference.fittingMode ?? 'HYPERBOLIC'
        this.request.initialOffsetSteps = preference.initialOffsetSteps ?? 4
        // this.request.rSquaredThreshold
        this.request.stepSize = preference.stepSize ?? 100
        this.request.totalNumberOfAttempts = preference.totalNumberOfAttempts ?? 1
        this.request.backlashCompensation.mode = preference.backlashCompensation.mode ?? 'NONE'
        this.request.backlashCompensation.backlashIn = preference.backlashCompensation.backlashIn ?? 0
        this.request.backlashCompensation.backlashOut = preference.backlashCompensation.backlashOut ?? 0

        if (this.camera.id) {
            const cameraPreference = this.preference.cameraPreference(this.camera).get()
            Object.assign(this.request.capture, this.preference.cameraStartCaptureForAutoFocus(this.camera).get(cameraPreference))

            if (this.camera.connected) {
                updateCameraStartCaptureFromCamera(this.request.capture, this.camera)
            }
        }
    }

    savePreference() {
        this.preference.cameraStartCaptureForAutoFocus(this.camera).set(this.request.capture)

        const preference: AutoFocusPreference = {
            ...this.request
        }

        this.preference.autoFocusPreference.set(preference)
    }
}