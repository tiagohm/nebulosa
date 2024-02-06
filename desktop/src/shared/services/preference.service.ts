import { Injectable } from '@angular/core'
import { Camera, CameraStartCapture, EMPTY_CAMERA_START_CAPTURE } from '../types/camera.types'
import { FilterWheel, WheelPreference } from '../types/wheel.types'
import { LocalStorageService } from './local-storage.service'

@Injectable({ providedIn: 'root' })
export class PreferenceService {

    constructor(private storage: LocalStorageService) { }

    // WHEEL.

    wheelPreferenceGet(wheel: FilterWheel) {
        return this.storage.get<WheelPreference>(`wheel.${wheel.name}`, {})
    }

    wheelPreferenceSet(wheel: FilterWheel, preference: WheelPreference) {
        this.storage.set(`wheel.${wheel.name}`, preference)
    }

    // FLAT WIZARD.

    flatWizardCameraGet(camera: Camera, value: CameraStartCapture = EMPTY_CAMERA_START_CAPTURE) {
        return this.storage.get(`flatWizard.camera.${camera.name}`, value)
    }

    flatWizardCameraSet(camera: Camera, capture?: CameraStartCapture) {
        this.storage.set(`flatWizard.camera.${camera.name}`, capture)
    }
}