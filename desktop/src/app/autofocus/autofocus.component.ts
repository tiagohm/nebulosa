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

    cameraConnect(camera?: Camera) {
        camera ??= this.camera

        if (camera.id) {
            if (camera.connected) {
                this.api.cameraDisconnect(camera)
            } else {
                this.api.cameraConnect(camera)
            }
        }
    }

    async focuserChanged() {
        if (this.focuser.id) {
            const focuser = await this.api.focuser(this.focuser.id)
            Object.assign(this.focuser, focuser)
        }
    }

    focuserConnect(focuser?: Focuser) {
        focuser ??= this.focuser

        if (focuser.id) {
            if (focuser.connected) {
                this.api.focuserDisconnect(focuser)
            } else {
                this.api.focuserConnect(focuser)
            }
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