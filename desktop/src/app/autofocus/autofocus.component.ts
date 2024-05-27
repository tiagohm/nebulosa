import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AutoFocusPreference, AutoFocusRequest } from '../../shared/types/autofocus.type'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { EMPTY_FOCUSER, Focuser } from '../../shared/types/focuser.types'
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
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE)
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

    stop() {
        return this.api.tppaStop(this.camera)
    }

    openCameraImage() {
        return this.browserWindow.openCameraImage(this.camera)
    }

    private loadPreference() {
        const preference = this.preference.autoFocusPreference.get()

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
        }

        this.preference.autoFocusPreference.set(preference)
    }
}